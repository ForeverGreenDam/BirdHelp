package com.greendam.birdhelp.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <p>
 * 会员套餐响应对象，用于套餐列表展示。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPlanVO {

    /**
     * 套餐 ID。
     */
    private Long id;

    /**
     * 套餐名称，如 "月卡"、"季卡"、"年卡"。
     */
    private String name;

    /**
     * 会员等级：{@code 1} - 月卡，{@code 2} - 季卡，{@code 3} - 年卡。
     */
    private Integer level;

    /**
     * 原价（展示用），单位：元。
     */
    private BigDecimal price;

    /**
     * 实际售价（支付用），单位：元。
     */
    private BigDecimal actualPrice;

    /**
     * 有效天数。
     */
    private Integer durationDays;

    /**
     * 每日生成次数上限。
     */
    private Integer dailyLimit;
}
