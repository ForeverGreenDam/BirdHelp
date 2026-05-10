package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.dto.*;
import com.greendam.birdhelp.model.vo.LoginVO;
import com.greendam.birdhelp.model.vo.UserInfoVO;
import com.greendam.birdhelp.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

/**
 * <p>
 * 用户模块接口控制器，提供注册、登录、个人信息管理及密码管理等功能。
 * </p>
 *
 * <h3>路径说明</h3>
 * <ul>
 *   <li>需要登录鉴权的接口：{@code GET|PUT /user/info}、{@code PUT /user/password}、{@code POST /user/avatar}</li>
 *   <li>无需登录鉴权的接口：{@code /user/login/**}、{@code /user/register/**}、{@code /user/send-code}、{@code /user/reset-password}</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private SysUserService sysUserService;

    /**
     * <p>发送短信或邮箱验证码。</p>
     *
     * <p>验证码为 6 位随机数字，保存在 Redis 中，有效期 5 分钟。
     * 根据 {@code type} 参数区分使用场景：{@code register} 注册、{@code login} 登录、{@code reset} 重置密码。</p>
     *
     * @param dto 包含发送目标（手机号或邮箱）及验证码类型的请求体
     * @return 操作成功无数据返回
     */
    @PostMapping("/send-code")
    public BaseResponse<Void> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        sysUserService.sendVerifyCode(dto);
        return BaseResponse.success();
    }

    /**
     * <p>手机号注册。</p>
     *
     * <p>使用手机号 + 验证码完成注册。密码经 BCrypt 加密后存储。</p>
     *
     * @param dto 包含手机号、验证码、用户名、密码、昵称的请求体
     * @return 注册成功无数据返回
     * @throws com.greendam.birdhelp.exception.BusinessException 手机号已注册、用户名已存在或验证码校验失败时抛出
     */
    @PostMapping("/register/phone")
    public BaseResponse<Void> registerByPhone(@Valid @RequestBody PhoneRegisterDTO dto) {
        sysUserService.registerByPhone(dto);
        return BaseResponse.success();
    }

    /**
     * <p>邮箱注册。</p>
     *
     * <p>使用邮箱 + 验证码完成注册。密码经 BCrypt 加密后存储。</p>
     *
     * @param dto 包含邮箱、验证码、用户名、密码、昵称的请求体
     * @return 注册成功无数据返回
     * @throws com.greendam.birdhelp.exception.BusinessException 邮箱已注册、用户名已存在或验证码校验失败时抛出
     */
    @PostMapping("/register/email")
    public BaseResponse<Void> registerByEmail(@Valid @RequestBody EmailRegisterDTO dto) {
        sysUserService.registerByEmail(dto);
        return BaseResponse.success();
    }

    /**
     * <p>密码登录。</p>
     *
     * <p>支持使用手机号、邮箱或用户名作为登录账号。</p>
     *
     * @param dto 包含账号和密码的请求体
     * @return 登录成功返回 JWT Token 及用户信息
     * @throws com.greendam.birdhelp.exception.BusinessException 用户不存在、账号被禁用或密码错误时抛出
     */
    @PostMapping("/login/password")
    public BaseResponse<LoginVO> loginByPassword(@Valid @RequestBody PasswordLoginDTO dto) {
        LoginVO result = sysUserService.loginByPassword(dto);
        return BaseResponse.success(result);
    }

    /**
     * <p>短信验证码登录（预留，暂未实现）。</p>
     *
     * @return 暂时返回空成功响应
     */
    @PostMapping("/login/sms")
    public BaseResponse<Void> loginBySms() {
        return BaseResponse.success();
    }

    /**
     * <p>微信登录（预留，暂未实现）。</p>
     *
     * @return 暂时返回空成功响应
     */
    @PostMapping("/login/wechat")
    public BaseResponse<Void> loginByWechat() {
        return BaseResponse.success();
    }

    /**
     * <p>获取当前登录用户的个人信息。</p>
     *
     * <p>需要携带有效的 JWT Token。</p>
     *
     * @return 用户信息视图对象
     * @throws com.greendam.birdhelp.exception.BusinessException 用户不存在时抛出
     */
    @GetMapping("/info")
    public BaseResponse<UserInfoVO> getUserInfo() {
        Long userId = BaseContext.getCurrentId();
        UserInfoVO vo = sysUserService.getUserInfo(userId);
        return BaseResponse.success(vo);
    }

    /**
     * <p>修改当前登录用户的个人信息。</p>
     *
     * <p>支持修改昵称、头像地址、性别和出生日期。仅更新传入的非空字段。</p>
     *
     * @param dto 包含可修改字段的请求体（所有字段均为可选）
     * @return 操作成功无数据返回
     * @throws com.greendam.birdhelp.exception.BusinessException 用户不存在时抛出
     */
    @PutMapping("/info")
    public BaseResponse<Void> updateUserInfo(@Valid @RequestBody UpdateUserInfoDTO dto) {
        Long userId = BaseContext.getCurrentId();
        sysUserService.updateUserInfo(userId, dto);
        return BaseResponse.success();
    }

    /**
     * <p>修改当前登录用户的密码。</p>
     *
     * <p>需要校验原密码正确后才能更新为新密码。</p>
     *
     * @param dto 包含原密码和新密码的请求体
     * @return 操作成功无数据返回
     * @throws com.greendam.birdhelp.exception.BusinessException 用户不存在或原密码错误时抛出
     */
    @PutMapping("/password")
    public BaseResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        Long userId = BaseContext.getCurrentId();
        sysUserService.updatePassword(userId, dto);
        return BaseResponse.success();
    }

    /**
     * <p>通过验证码重置密码。</p>
     *
     * <p>适用于忘记密码场景，无需登录即可调用。根据手机号或邮箱定位用户并校验验证码后重置密码。</p>
     *
     * @param dto 包含手机号/邮箱、验证码及新密码的请求体
     * @return 操作成功无数据返回
     * @throws com.greendam.birdhelp.exception.BusinessException 用户不存在或验证码校验失败时抛出
     */
    @PostMapping("/reset-password")
    public BaseResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        sysUserService.resetPassword(dto);
        return BaseResponse.success();
    }

    /**
     * <p>上传用户头像。</p>
     *
     * <p>接收 multipart 文件上传至阿里云 OSS，并将返回的 URL 写入用户记录的 {@code avatar} 字段。</p>
     *
     * @param file 头像图片文件（multipart/form-data，字段名 {@code file}）
     * @return 头像文件的 OSS 访问 URL
     * @throws IOException 读取文件字节流失败时抛出
     */
    @PostMapping("/avatar")
    public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        Long userId = BaseContext.getCurrentId();
        String url = sysUserService.uploadAvatar(userId, file.getBytes(), file.getOriginalFilename());
        return BaseResponse.success(url);
    }
}
