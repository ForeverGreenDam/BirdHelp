package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户会员信息更新 DTO，用于接收管理员修改用户会员等级及过期时间的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code userId} - 目标用户 ID，不能为空</li>
 *   <li>{@code memberLevel} - 会员等级，不能为空。例如 {@code 0} - 普通用户，{@code 1} - 会员</li>
 *   <li>{@code memberExpireAt} - 会员过期时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class UserQuotaMemberUpdateDTO {
    /**
     * 目标用户 ID，不能为空。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 会员等级，不能为空。例如 {@code 0} - 普通用户，{@code 1} - 会员。
     */
    @NotNull(message = "会员等级不能为空")
    private Integer memberLevel;

    /**
     * 会员过期时间。
     */
    private LocalDateTime memberExpireAt;
}
