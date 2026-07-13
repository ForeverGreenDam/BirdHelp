package com.greendam.birdhelp.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.common.utils.AliOssUtil;
import com.greendam.birdhelp.common.utils.AliSmsUtil;
import com.greendam.birdhelp.common.utils.JwtUtil;
import com.greendam.birdhelp.common.utils.MailUtil;
import com.greendam.birdhelp.constant.JwtClaimsConstant;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.SysUserMapper;
import com.greendam.birdhelp.model.dto.*;
import com.greendam.birdhelp.model.dto.admin.AdminLoginDTO;
import com.greendam.birdhelp.model.dto.admin.AdminUserUpdateDTO;
import com.greendam.birdhelp.model.entity.SysUser;
import com.greendam.birdhelp.model.vo.LoginVO;
import com.greendam.birdhelp.model.vo.UserInfoVO;
import com.greendam.birdhelp.model.vo.admin.AdminUserVO;
import com.greendam.birdhelp.properties.JwtProperties;
import com.greendam.birdhelp.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * 用户服务实现类，继承 MyBatis-Plus {@link ServiceImpl} 提供通用 CRUD 实现。
 * </p>
 *
 * <h3>核心依赖</h3>
 * <ul>
 *   <li>{@link StringRedisTemplate} — 验证码存储（5 分钟过期）</li>
 *   <li>{@link JwtProperties} — JWT 签发参数（密钥、TTL）</li>
 *   <li>{@link AliOssUtil} — 头像文件上传至阿里云 OSS</li>
 *   <li>{@link BCrypt} (Hutool) — 密码加密与校验</li>
 * </ul>
 *
 * @author ForeverGreenDam
 * @see SysUserService
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private AliOssUtil aliOssUtil;

    @Resource
    private MailUtil mailUtil;

    /**
     * Redis 短信发送限制键前缀
     */
    private static final String SMS_LIMIT_IP_PREFIX = "sms_limit:ip:";

    /** Redis 验证码键前缀 */
    private static final String VERIFY_CODE_PREFIX = "verify_code:";
    private static final String SMS_LIMIT_PHONE_MINUTE_PREFIX = "sms_limit:phone:";
    private static final String SMS_LIMIT_PHONE_HOUR_SUFFIX = ":hour";
    private static final String SMS_LIMIT_PHONE_DAY_SUFFIX = ":day";
    /**
     * 发送频率限制常量
     */
    private static final int IP_LIMIT_PER_MINUTE = 5;      // 同一IP每分钟最多5次
    private static final int PHONE_LIMIT_PER_MINUTE = 1;    // 同一手机号每分钟最多1次
    private static final int PHONE_LIMIT_PER_HOUR = 5;      // 同一手机号每小时最多5次
    private static final int PHONE_LIMIT_PER_DAY = 10;      // 同一手机号每天最多10次
    @Resource
    private AliSmsUtil aliSmsUtil;

    // ==================== 公开方法 ====================

    /**
     * <p>生成 6 位随机验证码并存入 Redis。</p>
     *
     * <p>Key 格式：{@code verify_code:{type}:{target}}，有效期 5 分钟。
     * 手机号场景调用阿里云短信服务发送，邮箱场景使用邮件发送。</p>
     *
     * @param dto 包含发送目标及验证码类型的请求体
     */
    @Override
    public void sendVerifyCode(SendCodeDTO dto) {
        // 1. 检查发送频率限制（防刷）
        checkSendFrequency(dto.getTarget());

        // 2. 生成验证码
        String code = RandomUtil.randomNumbers(6);
        String key = VERIFY_CODE_PREFIX + dto.getType() + ":" + dto.getTarget();
        stringRedisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));

        // 3. 判断是邮箱还是手机号，调用不同发送方式
        if (MailUtil.isEmail(dto.getTarget())) {
            mailUtil.sendVerifyCode(dto.getTarget(), code, dto.getType());
        } else {
            // 调用阿里云短信服务
            boolean success = aliSmsUtil.sendVerifyCode(dto.getTarget(), code);
            if (!success) {
                throw new BusinessException(ErrorCode.SMS_SEND_ERROR);
            }
            log.info("向手机号 {} 发送验证码成功 (类型: {})", dto.getTarget(), dto.getType());
        }

        // 4. 记录发送次数（用于防刷）
        incrementSendCount(dto.getTarget());
    }

    /**
     * <p>手机号注册。</p>
     *
     * <ol>
     *   <li>校验手机号未被注册</li>
     *   <li>校验用户名未被占用</li>
     *   <li>校验验证码有效性</li>
     *   <li>BCrypt 加密密码后保存用户</li>
     *   <li>清除已使用的验证码</li>
     * </ol>
     *
     * @param dto 手机号注册请求体
     * @throws BusinessException 手机号已注册、用户名已存在或验证码错误时抛出
     */
    @Override
    public void registerByPhone(PhoneRegisterDTO dto) {
        checkPhoneNotExists(dto.getPhone());
        checkUsernameNotExists(dto.getUsername());
        consumeVerifyCode("register", dto.getPhone(), dto.getCode());

        SysUser user = new SysUser();
        user.setPhone(dto.getPhone());
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setStatus(1);
        user.setUserType(1);
        save(user);

        log.info("手机号注册成功: {}", dto.getPhone());
    }

    /**
     * <p>邮箱注册。</p>
     *
     * <ol>
     *   <li>校验邮箱未被注册</li>
     *   <li>校验用户名未被占用</li>
     *   <li>校验验证码有效性</li>
     *   <li>BCrypt 加密密码后保存用户</li>
     *   <li>清除已使用的验证码</li>
     * </ol>
     *
     * @param dto 邮箱注册请求体
     * @throws BusinessException 邮箱已注册、用户名已存在或验证码错误时抛出
     */
    @Override
    public void registerByEmail(EmailRegisterDTO dto) {
        checkEmailNotExists(dto.getEmail());
        checkUsernameNotExists(dto.getUsername());
        consumeVerifyCode("register", dto.getEmail(), dto.getCode());

        SysUser user = new SysUser();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setStatus(1);
        user.setUserType(1);
        save(user);

        log.info("邮箱注册成功: {}", dto.getEmail());
    }

    /**
     * <p>密码登录。</p>
     *
     * <p>使用手机号、邮箱或用户名作为账号进行查询匹配，依次校验账号状态和密码。
     * 认证通过后签发 JWT 令牌并返回用户信息。</p>
     *
     * @param dto 包含账号和密码的登录请求体
     * @return 登录结果，包含 JWT Token 和用户信息
     * @throws BusinessException 错误码：
     *         <ul>
     *           <li>{@code USER_NOT_FOUND(40001)} — 账号不存在</li>
     *           <li>{@code USER_DISABLED(40007)} — 账号已被禁用</li>
     *           <li>{@code PASSWORD_ERROR(40005)} — 密码错误</li>
     *         </ul>
     */
    @Override
    public LoginVO loginByPassword(PasswordLoginDTO dto) {
        SysUser user = lambdaQuery()
                .eq(SysUser::getPhone, dto.getAccount())
                .or()
                .eq(SysUser::getEmail, dto.getAccount())
                .or()
                .eq(SysUser::getUsername, dto.getAccount())
                .one();

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String token = generateToken(user.getId());
        return LoginVO.builder()
                .token(token)
                .userInfo(toUserInfoVO(user))
                .build();
    }

    /**
     * <p>短信验证码登录。</p>
     *
     * <p>使用手机号 + 验证码登录。若手机号未注册则自动创建新用户。</p>
     *
     * @param dto 包含手机号和验证码的请求体
     * @return 登录结果，包含 JWT Token 和用户信息
     * @throws BusinessException 错误码：
     *                           <ul>
     *                             <li>{@code USER_DISABLED(40007)} — 账号已被禁用</li>
     *                             <li>{@code VERIFY_CODE_ERROR(40006)} — 验证码错误或已过期</li>
     *                           </ul>
     */
    @Override
    public LoginVO loginBySms(SmsLoginDTO dto) {
        // 1. 根据手机号查找用户
        SysUser user = lambdaQuery()
                .eq(SysUser::getPhone, dto.getPhone())
                .one();

        // 2. 用户不存在则自动注册
        if (user == null) {
            user = new SysUser();
            user.setPhone(dto.getPhone());
            user.setUsername("user_" + dto.getPhone());
            user.setNickname("用户" + dto.getPhone().substring(7));
            user.setStatus(1);
            user.setUserType(1);
            save(user);
            log.info("短信登录自动注册: {}", dto.getPhone());
        }

        // 3. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        // 4. 消费验证码
        consumeVerifyCode("login", dto.getPhone(), dto.getCode());

        // 5. 签发JWT
        String token = generateToken(user.getId());
        return LoginVO.builder()
                .token(token)
                .userInfo(toUserInfoVO(user))
                .build();
    }

    /**
     * <p>根据用户 ID 获取个人信息。</p>
     *
     * @param userId 用户 ID
     * @return 用户信息视图对象
     * @throws BusinessException {@code USER_NOT_FOUND(40001)} — 用户不存在
     */
    @Override
    public UserInfoVO getUserInfo(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toUserInfoVO(user);
    }

    /**
     * <p>更新用户个人信息。</p>
     *
     * <p>仅对传入的非空字段进行更新：{@code nickname}、{@code avatar}、{@code sex}、{@code birthday}。
     * 未传入的字段保持原值不变。</p>
     *
     * @param userId 当前用户 ID
     * @param dto   包含可修改字段的请求体
     * @throws BusinessException {@code USER_NOT_FOUND(40001)} — 用户不存在
     */
    @Override
    public void updateUserInfo(Long userId, UpdateUserInfoDTO dto) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getSex() != null) {
            user.setSex(dto.getSex());
        }
        if (dto.getBirthday() != null) {
            user.setBirthday(LocalDate.parse(dto.getBirthday()));
        }
        updateById(user);
    }

    /**
     * <p>修改密码（已登录状态下）。</p>
     *
     * <p>先校验原密码是否正确，校验通过后以 BCrypt 加密新密码并更新。</p>
     *
     * @param userId 当前用户 ID
     * @param dto   包含原密码和新密码的请求体
     * @throws BusinessException 错误码：
     *         <ul>
     *           <li>{@code USER_NOT_FOUND(40001)} — 用户不存在</li>
     *           <li>{@code OLD_PASSWORD_ERROR(40008)} — 原密码错误</li>
     *         </ul>
     */
    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        updateById(user);
    }

    /**
     * <p>通过验证码重置密码（忘记密码场景）。</p>
     *
     * <p>根据手机号或邮箱定位用户，校验验证码后更新为新密码，并清除已使用的验证码。</p>
     *
     * @param dto 包含手机号/邮箱、验证码及新密码的请求体
     * @throws BusinessException 错误码：
     *         <ul>
     *           <li>{@code USER_NOT_FOUND(40001)} — 未找到匹配用户</li>
     *           <li>{@code VERIFY_CODE_ERROR(40006)} — 验证码错误或已过期</li>
     *         </ul>
     */
    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        SysUser user = lambdaQuery()
                .eq(SysUser::getPhone, dto.getAccount())
                .or()
                .eq(SysUser::getEmail, dto.getAccount())
                .one();
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        consumeVerifyCode("reset", dto.getAccount(), dto.getCode());

        user.setPassword(BCrypt.hashpw(dto.getNewPassword()));
        updateById(user);
        log.info("重置密码成功: {}", dto.getAccount());
    }

    @Override
    public LoginVO adminLogin(AdminLoginDTO dto) {
        SysUser user = lambdaQuery()
                .eq(SysUser::getPhone, dto.getAccount())
                .or()
                .eq(SysUser::getEmail, dto.getAccount())
                .or()
                .eq(SysUser::getUsername, dto.getAccount())
                .one();

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getUserType() == null || user.getUserType() != 2) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "非管理员账号，无法登录后台");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.USER_TYPE, user.getUserType());
        claims.put(JwtClaimsConstant.USER_NAME, user.getUsername());
        String token = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claims);

        return LoginVO.builder()
                .token(token)
                .userInfo(toUserInfoVO(user))
                .build();
    }

    @Override
    public Page<AdminUserVO> adminListUsers(int page, int size, String username, String phone,
                                            String email, Integer status, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(username != null, SysUser::getUsername, username)
                .like(phone != null, SysUser::getPhone, phone)
                .like(email != null, SysUser::getEmail, email)
                .eq(status != null, SysUser::getStatus, status)
                .ge(startDate != null, SysUser::getCreateTime, startDate != null ? startDate.atStartOfDay() : null)
                .le(endDate != null, SysUser::getCreateTime, endDate != null ? endDate.plusDays(1).atStartOfDay() : null)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> userPage = page(Page.of(page, size), wrapper);
        Page<AdminUserVO> voPage = new Page<>(page, size, userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(this::toAdminUserVO).toList());
        return voPage;
    }

    @Override
    public AdminUserVO adminGetUser(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toAdminUserVO(user);
    }

    @Override
    public void adminUpdateStatus(Long userId, Integer status) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        updateById(user);
        log.info("管理员 {} 更新用户 {} 状态为 {}", userId, user.getUsername(), status);
    }

    @Override
    public void adminUpdateUser(Long userId, AdminUserUpdateDTO dto) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getSex() != null) user.setSex(dto.getSex());
        if (dto.getBirthday() != null && !dto.getBirthday().isEmpty()) {
            user.setBirthday(LocalDate.parse(dto.getBirthday()));
        }
        updateById(user);
    }

    @Override
    public void adminResetPassword(Long userId, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setPassword(BCrypt.hashpw(newPassword));
        updateById(user);
        log.info("管理员重置用户 {} 密码", user.getUsername());
    }

    @Override
    public void adminSetUserRole(Long userId, Integer userType) {
        if (userType != 1 && userType != 2) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户类型必须为 1（普通用户）或 2（管理员）");
        }
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setUserType(userType);
        updateById(user);
        log.info("管理员更新用户 {} 角色为 {}", user.getUsername(), userType == 2 ? "管理员" : "普通用户");
    }

    /**
     * <p>上传头像文件至阿里云 OSS 并更新用户记录。</p>
     *
     * <p>文件存储路径格式：{@code avatar/{32位uuid}.{后缀名}}。
     * 上传成功后自动将 OSS URL 写入用户 {@code avatar} 字段。</p>
     *
     * @param userId           当前用户 ID
     * @param fileBytes        文件字节数组
     * @param originalFilename 原始文件名，用于提取扩展名
     * @return OSS 文件访问 URL
     */
    @Override
    public String uploadAvatar(Long userId, byte[] fileBytes, String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = "avatar/" + UUID.randomUUID().toString().replace("-", "") + ext;
        String url = aliOssUtil.upload(fileBytes, objectName);

        SysUser user = getById(userId);
        if (user != null) {
            user.setAvatar(url);
            updateById(user);
        }
        return url;
    }

    // ==================== 内部方法 ====================

    /**
     * 检查发送频率是否超限（防刷机制）
     *
     * @param target 手机号或邮箱
     * @throws BusinessException 频率超限时抛出
     */
    private void checkSendFrequency(String target) {
        if (MailUtil.isEmail(target)) {
            // 邮箱不限制频率
            return;
        }

        // 1. 检查手机号分钟级限制
        String phoneMinuteKey = SMS_LIMIT_PHONE_MINUTE_PREFIX + target;
        String minuteValue = stringRedisTemplate.opsForValue().get(phoneMinuteKey);
        if (minuteValue != null) {
            throw new BusinessException(ErrorCode.SMS_TOO_FREQUENT, "验证码已发送，请1分钟后再试");
        }

        // 2. 检查手机号小时级限制
        String phoneHourKey = SMS_LIMIT_PHONE_MINUTE_PREFIX + target + SMS_LIMIT_PHONE_HOUR_SUFFIX;
        Long phoneHourCount = stringRedisTemplate.opsForValue().increment(phoneHourKey);
        if (phoneHourCount == 1) {
            stringRedisTemplate.expire(phoneHourKey, Duration.ofHours(1));
        }
        if (phoneHourCount > PHONE_LIMIT_PER_HOUR) {
            throw new BusinessException(ErrorCode.SMS_TOO_FREQUENT, "该手机号1小时内发送次数过多");
        }

        // 3. 检查手机号天级限制
        String phoneDayKey = SMS_LIMIT_PHONE_MINUTE_PREFIX + target + SMS_LIMIT_PHONE_DAY_SUFFIX;
        Long phoneDayCount = stringRedisTemplate.opsForValue().increment(phoneDayKey);
        if (phoneDayCount == 1) {
            stringRedisTemplate.expire(phoneDayKey, Duration.ofDays(1));
        }
        if (phoneDayCount > PHONE_LIMIT_PER_DAY) {
            throw new BusinessException(ErrorCode.SMS_SEND_LIMIT_EXCEEDED);
        }
    }

    /**
     * 记录发送次数（发送成功后调用）
     *
     * @param target 手机号或邮箱
     */
    private void incrementSendCount(String target) {
        if (MailUtil.isEmail(target)) {
            return;
        }

        // 设置分钟级标记（1分钟过期）
        String phoneMinuteKey = SMS_LIMIT_PHONE_MINUTE_PREFIX + target;
        stringRedisTemplate.opsForValue().set(phoneMinuteKey, "1", Duration.ofMinutes(1));
    }

    /**
     * 签发 JWT 令牌。
     *
     * @param userId 用户 ID，存入 Token 的 {@code id} 声明
     * @return 签发的 JWT 字符串
     */
    private String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        return JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
    }

    /**
     * 消费验证码：原子性地取出并删除，匹配失败则抛出异常。
     *
     * <p>使用 Redis {@code GETDEL} 命令一次性完成读取和删除，
     * 杜绝两步操作之间的并发重放窗口。</p>
     *
     * @param type   验证码类型（register / login / reset）
     * @param target 手机号或邮箱
     * @param code   用户提交的验证码
     * @throws BusinessException {@code VERIFY_CODE_ERROR(40006)} — 验证码不存在或不匹配
     */
    private void consumeVerifyCode(String type, String target, String code) {
        String key = VERIFY_CODE_PREFIX + type + ":" + target;
        String storedCode = stringRedisTemplate.opsForValue().getAndDelete(key);
        if (storedCode == null || !storedCode.equals(code)) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_ERROR);
        }
    }

    /**
     * 检查手机号是否已被注册。
     *
     * @param phone 手机号
     * @throws BusinessException {@code PHONE_EXISTS(40003)} — 手机号已注册
     */
    private void checkPhoneNotExists(String phone) {
        long count = lambdaQuery().eq(SysUser::getPhone, phone).count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }
    }

    /**
     * 检查邮箱是否已被注册。
     *
     * @param email 邮箱
     * @throws BusinessException {@code EMAIL_EXISTS(40004)} — 邮箱已注册
     */
    private void checkEmailNotExists(String email) {
        long count = lambdaQuery().eq(SysUser::getEmail, email).count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
    }

    /**
     * 检查用户名是否已被占用。
     *
     * @param username 用户名
     * @throws BusinessException {@code USERNAME_EXISTS(40002)} — 用户名已存在
     */
    private void checkUsernameNotExists(String username) {
        long count = lambdaQuery().eq(SysUser::getUsername, username).count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
    }

    /**
     * 将 {@link SysUser} 实体转换为 {@link UserInfoVO} 视图对象。
     *
     * @param user 用户实体
     * @return 用户信息视图（不含密码等敏感字段）
     */
    private UserInfoVO toUserInfoVO(SysUser user) {
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .sex(user.getSex())
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .build();
    }

    private AdminUserVO toAdminUserVO(SysUser user) {
        return AdminUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .email(user.getEmail())
                .sex(user.getSex())
                .birthday(user.getBirthday())
                .userType(user.getUserType())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
