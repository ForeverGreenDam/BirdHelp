package com.greendam.birdhelp.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户信息视图对象，用于对外展示用户信息。
 * </p>
 *
 * <p>敏感字段（如密码、微信 UnionID 等）不会出现在此视图中。</p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class UserInfoVO {

    /** 用户 ID */
    private Long id;

    /** 登录用户名 */
    private String username;

    /** 用户昵称（显示名称） */
    private String nickname;

    /** 头像文件 URL */
    private String avatar;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 性别：0-未知，1-男，2-女 */
    private Integer sex;

    /** 出生日期 */
    private LocalDate birthday;

    /** 用户类型：1-普通学生，2-管理员 */
    private Integer userType;

    /** 账号状态：0-禁用，1-正常 */
    private Integer status;

    /** 注册时间（创建时间） */
    private LocalDateTime createTime;
}
