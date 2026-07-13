package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 修改额度配置 DTO，用于接收管理员修改额度配置的请求参数。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class QuotaConfigUpdateDTO {

    /**
     * 配置 ID，不能为空。
     */
    @NotNull(message = "配置ID不能为空")
    private Long id;

    /**
     * 每日生成次数上限。
     */
    private Integer dailyLimit;

    /**
     * 有效天数，{@code 0} 表示不限。
     */
    private Integer durationDays;
}
