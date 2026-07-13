package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 新增额度配置 DTO，用于接收管理员创建额度配置的请求参数。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class QuotaConfigCreateDTO {

    /**
     * 会员等级，不能为空，且不能与已有配置重复。
     */
    @NotNull(message = "会员等级不能为空")
    private Integer level;

    /**
     * 每日生成次数上限，不能为空。
     */
    @NotNull(message = "每日次数上限不能为空")
    private Integer dailyLimit;

    /**
     * 有效天数，{@code 0} 表示不限，不能为空。
     */
    @NotNull(message = "有效天数不能为空")
    private Integer durationDays;
}
