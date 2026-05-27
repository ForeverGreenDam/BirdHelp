package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 额度配置更新 DTO，用于接收管理员修改用户每日额度上限的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 额度配置 ID，不能为空</li>
 *   <li>{@code dailyLimit} - 每日额度上限，不能为空</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class QuotaConfigUpdateDTO {
    /**
     * 额度配置 ID，不能为空。
     */
    @NotNull(message = "配置ID不能为空")
    private Long id;

    /**
     * 每日额度上限，不能为空。
     */
    @NotNull(message = "每日额度不能为空")
    private Integer dailyLimit;
}
