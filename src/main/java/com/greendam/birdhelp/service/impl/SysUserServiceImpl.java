package com.greendam.birdhelp.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.common.utils.AliOssUtil;
import com.greendam.birdhelp.common.utils.JwtUtil;
import com.greendam.birdhelp.constant.JwtClaimsConstant;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.SysUserMapper;
import com.greendam.birdhelp.model.dto.*;
import com.greendam.birdhelp.model.entity.SysUser;
import com.greendam.birdhelp.model.vo.LoginVO;
import com.greendam.birdhelp.model.vo.UserInfoVO;
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

    /** Redis 验证码键前缀 */
    private static final String VERIFY_CODE_PREFIX = "verify_code:";

    // ==================== 公开方法 ====================

    /**
     * <p>生成 6 位随机验证码并存入 Redis。</p>
     *
     * <p>Key 格式：{@code verify_code:{type}:{target}}，有效期 5 分钟。
     * 当前版本仅将验证码打印到日志，不实际发送短信或邮件。</p>
     *
     * @param dto 包含发送目标及验证码类型的请求体
     */
    @Override
    public void sendVerifyCode(SendCodeDTO dto) {
        String code = RandomUtil.randomNumbers(6);
        String key = VERIFY_CODE_PREFIX + dto.getType() + ":" + dto.getTarget();
        stringRedisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));
        log.info("向 {} 发送验证码: {} (类型: {})", dto.getTarget(), code, dto.getType());
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
}
