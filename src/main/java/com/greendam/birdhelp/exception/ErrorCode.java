package com.greendam.birdhelp.exception;

import lombok.Getter;

/**
 * <p>
 * 统一错误码枚举，定义了系统中所有业务错误的编码和描述。
 * </p>
 *
 * <h3>错误码分段</h3>
 * <table border="1">
 *   <caption>错误码分段规则</caption>
 *   <tr><th>范围</th><th>分类</th></tr>
 *   <tr><td>{@code 0}</td><td>成功</td></tr>
 *   <tr><td>{@code 40000-40999}</td><td>请求参数 / 业务校验错误</td></tr>
 *   <tr><td>{@code 40100-40199}</td><td>认证鉴权错误</td></tr>
 *   <tr><td>{@code 40300-40399}</td><td>访问禁止</td></tr>
 *   <tr><td>{@code 40400-40499}</td><td>数据未找到</td></tr>
 *   <tr><td>{@code 50000+}</td><td>系统内部错误</td></tr>
 * </table>
 *
 * @author ForeverGreenDam
 */
@Getter
public enum ErrorCode {

    // ==================== 通用错误码 ====================

    /** 操作成功 */
    SUCCESS(0, "ok"),

    /** 请求参数错误 */
    PARAMS_ERROR(40000, "请求参数错误"),

    /** 未登录，JWT 令牌缺失或无效 */
    NOT_LOGIN_ERROR(40100, "未登录"),

    /** 无权限访问该资源 */
    NOT_AUTH_ERROR(40101, "无权限访问"),

    /** 请求的数据不存在 */
    NOT_FOUND_ERROR(40400, "请求数据不存在"),

    /** 禁止访问 */
    FORBIDDEN_ERROR(40300, "禁止访问"),

    /** 系统内部错误 */
    SYSTEM_ERROR(50000, "系统内部错误"),

    /** 操作失败（通用） */
    OPERATION_ERROR(50001, "操作失败"),

    // ==================== 用户模块错误码 ====================

    /** 用户不存在 */
    USER_NOT_FOUND(40001, "用户不存在"),

    /** 用户名已被注册或占用 */
    USERNAME_EXISTS(40002, "用户名已存在"),

    /** 手机号已被注册 */
    PHONE_EXISTS(40003, "手机号已注册"),

    /** 邮箱已被注册 */
    EMAIL_EXISTS(40004, "邮箱已注册"),

    /** 登录密码错误 */
    PASSWORD_ERROR(40005, "密码错误"),

    /** 验证码错误或已过期 */
    VERIFY_CODE_ERROR(40006, "验证码错误或已过期"),

    /** 账号已被禁用，无法登录 */
    USER_DISABLED(40007, "账号已被禁用"),

    /** 修改密码时旧密码校验不通过 */
    OLD_PASSWORD_ERROR(40008, "原密码错误"),

    // ==================== 额度模块错误码 ====================

    /** 当日额度已用完 */
    QUOTA_EXCEEDED(40009, "今日额度已用完"),
    ;

    /**
     * 错误码数值。
     */
    private final int code;

    /**
     * 错误码描述信息，面向用户展示。
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
