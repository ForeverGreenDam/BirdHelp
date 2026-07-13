package com.greendam.birdhelp.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 会员订单响应对象，用于订单详情和列表展示。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberOrderVO {

    /**
     * 订单 ID。
     */
    private Long id;

    /**
     * 订单号。
     */
    private String orderNo;

    /**
     * 套餐 ID。
     */
    private Long planId;

    /**
     * 套餐名称（关联查询）。
     */
    private String planName;

    /**
     * 支付金额，单位：元。
     */
    private BigDecimal amount;

    /**
     * 支付方式：{@code 2} - 支付宝。
     */
    private Integer payType;

    /**
     * 订单状态：{@code 0} - 待支付，{@code 1} - 已支付，{@code 2} - 已过期。
     */
    private Integer status;

    /**
     * 支付宝交易号。
     */
    private String tradeNo;

    /**
     * 支付成功时间。
     */
    private LocalDateTime paidAt;

    /**
     * 订单过期时间。
     */
    private LocalDateTime expireAt;

    /**
     * 订单创建时间。
     */
    private LocalDateTime createTime;
}
