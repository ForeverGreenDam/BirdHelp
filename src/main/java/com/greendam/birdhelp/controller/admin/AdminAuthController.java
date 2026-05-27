package com.greendam.birdhelp.controller.admin;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.dto.admin.AdminLoginDTO;
import com.greendam.birdhelp.model.vo.LoginVO;
import com.greendam.birdhelp.service.SysUserService;
import com.greendam.birdhelp.service.admin.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 管理员认证控制器，提供后台管理登录接口。
 * </p>
 *
 * <h3>鉴权说明</h3>
 * <ul>
 *   <li>登录接口 {@code POST /admin/login} 无需 Token，使用 admin 密钥签发 JWT</li>
 *   <li>仅 {@code userType=2}（管理员）的用户允许登录后台</li>
 *   <li>其余 {@code /admin/**} 路径由 {@link com.greendam.birdhelp.interceptor.JwtTokenAdminInterceptor} 拦截校验</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminAuthController {

    @Resource
    private SysUserService sysUserService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * <p>管理员登录。</p>
     *
     * <p>使用手机号、邮箱或用户名作为账号进行登录，校验密码及管理员身份后签发 admin JWT 令牌。</p>
     *
     * @param dto 包含 {@code account}（手机号/邮箱/用户名）和 {@code password} 的请求体
     * @return 登录成功返回 {@link LoginVO}，包含 admin JWT Token 和用户信息
     * @throws com.greendam.birdhelp.exception.BusinessException 错误码：
     *                                                           <ul>
     *                                                             <li>{@code USER_NOT_FOUND(40001)} — 账号不存在</li>
     *                                                             <li>{@code NOT_AUTH_ERROR(40101)} — 非管理员账号</li>
     *                                                             <li>{@code USER_DISABLED(40007)} — 账号已被禁用</li>
     *                                                             <li>{@code PASSWORD_ERROR(40005)} — 密码错误</li>
     *                                                           </ul>
     */
    @PostMapping("/login")
    public BaseResponse<LoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        LoginVO result = sysUserService.adminLogin(dto);
        operationLogService.record(result.getUserInfo().getId(), result.getUserInfo().getUsername(),
                "LOGIN", "admin", "", "管理员登录");
        return BaseResponse.success(result);
    }
}
