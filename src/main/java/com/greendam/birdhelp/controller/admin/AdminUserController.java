package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.dto.admin.AdminUserUpdateDTO;
import com.greendam.birdhelp.model.vo.admin.AdminUserVO;
import com.greendam.birdhelp.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * <p>
 * 管理员用户管理控制器，提供用户列表查询、详情、封禁/启用、信息修改、密码重置、角色设置等功能。
 * </p>
 *
 * <h3>路径说明</h3>
 * <p>所有接口均需携带有效的 Admin JWT Token（请求头 {@code admin-token}）。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Resource
    private SysUserService sysUserService;

    /**
     * <p>分页查询用户列表，支持多条件筛选。</p>
     *
     * @param page      页码，默认 1
     * @param size      每页条数，默认 10
     * @param username  用户名模糊匹配（可选）
     * @param phone     手机号模糊匹配（可选）
     * @param email     邮箱模糊匹配（可选）
     * @param status    账号状态筛选：{@code 0}-禁用，{@code 1}-正常（可选）
     * @param startDate 注册时间起始日期（可选）
     * @param endDate   注册时间截止日期（可选）
     * @return 分页用户列表，按创建时间倒序
     */
    @GetMapping("/list")
    public BaseResponse<Page<AdminUserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Page<AdminUserVO> result = sysUserService.adminListUsers(page, size, username, phone, email, status, startDate, endDate);
        return BaseResponse.success(result);
    }

    /**
     * <p>查看用户详情。</p>
     *
     * @param id 用户 ID
     * @return 用户详细信息
     * @throws com.greendam.birdhelp.exception.BusinessException {@code USER_NOT_FOUND(40001)} — 用户不存在
     */
    @GetMapping("/{id}")
    public BaseResponse<AdminUserVO> detail(@PathVariable Long id) {
        AdminUserVO vo = sysUserService.adminGetUser(id);
        return BaseResponse.success(vo);
    }

    /**
     * <p>封禁或启用用户账号。</p>
     *
     * @param id     用户 ID
     * @param status 目标状态：{@code 0}-禁用，{@code 1}-正常
     * @return 操作成功无数据返回
     */
    @PutMapping("/{id}/status")
    public BaseResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        sysUserService.adminUpdateStatus(id, status);
        return BaseResponse.success();
    }

    /**
     * <p>管理员修改用户基本信息。</p>
     *
     * <p>仅更新传入的非空字段，未传入的字段保持原值不变。</p>
     *
     * @param id  用户 ID
     * @param dto 包含可修改字段的请求体（所有字段均为可选）
     * @return 操作成功无数据返回
     */
    @PutMapping("/{id}")
    public BaseResponse<Void> updateInfo(@PathVariable Long id, @RequestBody AdminUserUpdateDTO dto) {
        sysUserService.adminUpdateUser(id, dto);
        return BaseResponse.success();
    }

    /**
     * <p>管理员强制重置用户密码。</p>
     *
     * <p>不需要校验原密码，直接设置新密码。</p>
     *
     * @param id          用户 ID
     * @param newPassword 新密码
     * @return 操作成功无数据返回
     */
    @PutMapping("/{id}/password")
    public BaseResponse<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        sysUserService.adminResetPassword(id, newPassword);
        return BaseResponse.success();
    }

    /**
     * <p>设置或撤销用户的管理员角色。</p>
     *
     * @param id       用户 ID
     * @param userType 用户类型：{@code 1}-普通用户，{@code 2}-管理员
     * @return 操作成功无数据返回
     * @throws com.greendam.birdhelp.exception.BusinessException {@code PARAMS_ERROR(40000)} — userType 不是 1 或 2
     */
    @PutMapping("/{id}/role")
    public BaseResponse<Void> setRole(@PathVariable Long id, @RequestParam Integer userType) {
        sysUserService.adminSetUserRole(id, userType);
        return BaseResponse.success();
    }
}
