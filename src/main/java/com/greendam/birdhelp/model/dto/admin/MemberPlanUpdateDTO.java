package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * <p>
 * 修改会员套餐 DTO，用于接收管理员修改套餐的请求参数。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class MemberPlanUpdateDTO {

    /**
     * 套餐 ID，不能为空。
     */
    @NotNull(message = "套餐ID不能为空")
    private Long id;

    /**
     * 套餐名称。
     */
    private String name;

    /**
     * 原价（展示用）。
     */
    private BigDecimal price;

    /**
     * 实际售价（支付用）。
     */
    private BigDecimal actualPrice;
}
