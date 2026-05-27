package com.greendam.birdhelp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.model.dto.*;
import com.greendam.birdhelp.model.dto.admin.AdminLoginDTO;
import com.greendam.birdhelp.model.dto.admin.AdminUserUpdateDTO;
import com.greendam.birdhelp.model.entity.SysUser;
import com.greendam.birdhelp.model.vo.LoginVO;
import com.greendam.birdhelp.model.vo.UserInfoVO;
import com.greendam.birdhelp.model.vo.admin.AdminUserVO;

import java.time.LocalDate;

/**
 * <p>
 * 用户服务接口，继承 MyBatis-Plus {@link IService} 获得通用 CRUD 能力。
 * </p>
 *
 * <h3>密码安全</h3>
 * <p>所有密码均通过 BCrypt 算法加密存储，绝不以明文形式落库。</p>
 *
 * @author ForeverGreenDam
 * @see com.greendam.birdhelp.service.impl.SysUserServiceImpl
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * <p>发送验证码到指定手机号或邮箱。</p>
     *
     * <p>验证码为 6 位随机数字，写入 Redis 并设置 5 分钟过期。</p>
     *
     * @param dto 包含发送目标（手机号/邮箱）及类型（register/login/reset）的请求体
     */
    void sendVerifyCode(SendCodeDTO dto);

    /**
     * <p>手机号 + 验证码注册。</p>
     *
     * <p>校验手机号唯一性、用户名唯一性及验证码有效性后，创建用户并清除已使用的验证码。</p>
     *
     * @param dto 包含手机号、验证码、用户名、密码、昵称的请求体
     * @throws com.greendam.birdhelp.exception.BusinessException 错误码：
     *         <ul>
     *           <li>{@code PHONE_EXISTS(40003)} — 手机号已被注册</li>
     *           <li>{@code USERNAME_EXISTS(40002)} — 用户名已被占用</li>
     *           <li>{@code VERIFY_CODE_ERROR(40006)} — 验证码错误或已过期</li>
     *         </ul>
     */
    void registerByPhone(PhoneRegisterDTO dto);

    /**
     * <p>邮箱 + 验证码注册。</p>
     *
     * <p>校验邮箱唯一性、用户名唯一性及验证码有效性后，创建用户并清除已使用的验证码。</p>
     *
     * @param dto 包含邮箱、验证码、用户名、密码、昵称的请求体
     * @throws com.greendam.birdhelp.exception.BusinessException 错误码：
     *         <ul>
     *           <li>{@code EMAIL_EXISTS(40004)} — 邮箱已被注册</li>
     *           <li>{@code USERNAME_EXISTS(40002)} — 用户名已被占用</li>
     *           <li>{@code VERIFY_CODE_ERROR(40006)} — 验证码错误或已过期</li>
     *         </ul>
     */
    void registerByEmail(EmailRegisterDTO dto);

    /**
     * <p>密码登录。</p>
     *
     * <p>支持使用手机号、邮箱或用户名作为登录凭证。校验账号状态及密码后签发 JWT 令牌。</p>
     *
     * @param dto 包含 {@code account}（手机号/邮箱/用户名）和 {@code password} 的请求体
     * @return 登录成功返回 {@link LoginVO}，包含 JWT Token 和用户信息
     * @throws com.greendam.birdhelp.exception.BusinessException 错误码：
     *         <ul>
     *           <li>{@code USER_NOT_FOUND(40001)} — 账号不存在</li>
     *           <li>{@code USER_DISABLED(40007)} — 账号已被禁用</li>
     *           <li>{@code PASSWORD_ERROR(40005)} — 密码错误</li>
     *         </ul>
     */
    LoginVO loginByPassword(PasswordLoginDTO dto);

    /**
     * <p>根据用户 ID 查询个人信息。</p>
     *
     * @param userId 用户 ID
     * @return 用户信息视图对象
     * @throws com.greendam.birdhelp.exception.BusinessException {@code USER_NOT_FOUND(40001)} — 用户不存在
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * <p>更新用户个人信息。</p>
     *
     * <p>仅更新传入的非空字段（昵称、头像、性别、出生日期），未传入的字段保持不变。</p>
     *
     * @param userId 当前登录用户 ID
     * @param dto   包含可修改字段的请求体（字段均为可选）
     * @throws com.greendam.birdhelp.exception.BusinessException {@code USER_NOT_FOUND(40001)} — 用户不存在
     */
    void updateUserInfo(Long userId, UpdateUserInfoDTO dto);

    /**
     * <p>修改密码（需校验原密码）。</p>
     *
     * <p>适用于已登录用户自主修改密码的场景。</p>
     *
     * @param userId 当前登录用户 ID
     * @param dto   包含原密码和新密码的请求体
     * @throws com.greendam.birdhelp.exception.BusinessException 错误码：
     *         <ul>
     *           <li>{@code USER_NOT_FOUND(40001)} — 用户不存在</li>
     *           <li>{@code OLD_PASSWORD_ERROR(40008)} — 原密码错误</li>
     *         </ul>
     */
    void updatePassword(Long userId, UpdatePasswordDTO dto);

    /**
     * <p>通过验证码重置密码。</p>
     *
     * <p>适用于忘记密码场景。根据手机号或邮箱定位用户，校验验证码后更新为新密码。</p>
     *
     * @param dto 包含手机号/邮箱、验证码及新密码的请求体
     * @throws com.greendam.birdhelp.exception.BusinessException 错误码：
     *         <ul>
     *           <li>{@code USER_NOT_FOUND(40001)} — 未找到匹配该手机号/邮箱的用户</li>
     *           <li>{@code VERIFY_CODE_ERROR(40006)} — 验证码错误或已过期</li>
     *         </ul>
     */
    void resetPassword(ResetPasswordDTO dto);

    /**
     * 管理员登录。仅允许 userType=2 的用户登录，使用管理员 JWT 密钥签发令牌。
     */
    LoginVO adminLogin(AdminLoginDTO dto);

    Page<AdminUserVO> adminListUsers(int page, int size, String username, String phone, String email, Integer status, LocalDate startDate, LocalDate endDate);

    AdminUserVO adminGetUser(Long userId);

    void adminUpdateStatus(Long userId, Integer status);

    void adminUpdateUser(Long userId, AdminUserUpdateDTO dto);

    void adminResetPassword(Long userId, String newPassword);

    void adminSetUserRole(Long userId, Integer userType);

    /**
     * <p>上传用户头像至 OSS 并更新数据库记录。</p>
     *
     * <p>文件存储路径格式：{@code avatar/{uuid}.{ext}}。</p>
     *
     * @param userId           当前登录用户 ID
     * @param fileBytes        文件字节数组
     * @param originalFilename 原始文件名（用于提取扩展名）
     * @return 上传成功后文件的 OSS 访问 URL
     */
    String uploadAvatar(Long userId, byte[] fileBytes, String originalFilename);
}
