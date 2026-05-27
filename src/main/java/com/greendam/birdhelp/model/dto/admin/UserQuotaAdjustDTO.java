package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 用户额度调整 DTO，用于接收管理员对指定用户进行额度增减的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code userId} - 目标用户 ID，不能为空</li>
 *   <li>{@code changeAmount} - 调整数量（正数为增加，负数为减少），不能为空</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class UserQuotaAdjustDTO {
    /**
     * 目标用户 ID，不能为空。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 调整数量（正数为增加，负数为减少），不能为空。
     */
    @NotNull(message = "调整数量不能为空")
    private Integer changeAmount;
}
