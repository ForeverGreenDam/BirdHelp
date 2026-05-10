package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDate;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户信息表实体类，映射数据表 {@code sys_user}。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code userType}: {@code 1} - 普通学生，{@code 2} - 管理员</li>
 *   <li>{@code status}: {@code 0} - 禁用，{@code 1} - 正常</li>
 *   <li>{@code sex}: {@code 0} - 未知，{@code 1} - 男，{@code 2} - 女</li>
 * </ul>
 *
 * <p>审计字段（创建时间、更新人等）继承自 {@link BaseEntity}。</p>
 *
 * @author ForeverGreenDam
 * @see BaseEntity
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value ="sys_user")
@Data
public class SysUser extends BaseEntity {

    /**
     * 主键用户 ID，数据库自增。
     */
    @TableId
    private Long id;

    /**
     * 租户 ID，多租户预留字段。
     */
    private Long tenantId;

    /**
     * 登录用户名，全局唯一，长度不超过 50 位。
     */
    private String username;

    /**
     * 用户昵称（显示名称）。
     */
    private String nickname;

    /**
     * 加密后的密码，使用 BCrypt 算法加密，不允许明文存储。
     */
    private String password;

    /**
     * 头像文件 URL 地址。
     */
    private String avatar;

    /**
     * 手机号，全局唯一。
     */
    private String phone;

    /**
     * 邮箱地址，全局唯一。
     */
    private String email;

    /**
     * 性别：{@code 0} - 未知，{@code 1} - 男，{@code 2} - 女。
     */
    private Integer sex;

    /**
     * 出生日期。
     */
    private LocalDate birthday;

    /**
     * 用户类型：{@code 1} - 普通学生，{@code 2} - 管理员。
     */
    private Integer userType;

    /**
     * 账号状态：{@code 0} - 禁用，{@code 1} - 正常。
     */
    private Integer status;

    /**
     * 微信 OpenID，用于微信登录关联。
     */
    private String wxOpenid;

    /**
     * 微信 UnionID，用于跨应用用户识别。
     */
    private String wxUnionid;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
