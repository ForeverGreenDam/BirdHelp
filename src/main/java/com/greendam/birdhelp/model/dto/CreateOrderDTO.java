package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 创建会员订单请求体。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class CreateOrderDTO {

    /**
     * 套餐 ID，不可为空。
     */
    @NotNull(message = "套餐ID不能为空")
    private Long planId;
}
