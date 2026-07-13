package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * <p>
 * 新增会员套餐 DTO，用于接收管理员创建套餐的请求参数。
 * </p>
 *
 * <p>有效天数和每日生成次数上限由 {@code quota_config} 按 level 关联定义，不在本 DTO 中传入。</p>
 *
 * @author ForeverGreenDam
 */
@Data
public class MemberPlanCreateDTO {

    /**
     * 套餐名称，不能为空。
     */
    @NotBlank(message = "套餐名称不能为空")
    private String name;

    /**
     * 会员等级，关联 quota_config.level，不能为空。
     */
    @NotNull(message = "会员等级不能为空")
    private Integer level;

    /**
     * 原价（展示用），不能为空。
     */
    @NotNull(message = "原价不能为空")
    private BigDecimal price;

    /**
     * 实际售价（支付用），不能为空。
     */
    @NotNull(message = "售价不能为空")
    private BigDecimal actualPrice;
}
