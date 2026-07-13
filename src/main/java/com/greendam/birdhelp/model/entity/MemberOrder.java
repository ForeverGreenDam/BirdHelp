package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 会员订单表实体类，映射数据表 {@code member_order}。
 * 记录用户购买会员套餐的订单信息，包含支付状态和支付宝交易号。
 * </p>
 *
 * <h3>订单状态</h3>
 * <ul>
 *   <li>{@code 0} - 待支付</li>
 *   <li>{@code 1} - 已支付</li>
 *   <li>{@code 2} - 已过期（创建后15分钟未支付）</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "member_order")
@Data
public class MemberOrder extends BaseEntity {

    /**
     * 主键 ID，数据库自增。
     */
    @TableId
    private Long id;

    /**
     * 订单号（唯一），用于关联支付宝支付。
     */
    private String orderNo;

    /**
     * 用户 ID，关联 {@code sys_user.id}。
     */
    private Long userId;

    /**
     * 套餐 ID，关联 {@code member_plan.id}。
     */
    private Long planId;

    /**
     * 支付金额，单位：元。
     */
    private BigDecimal amount;

    /**
     * 支付方式：{@code 1} - 微信支付，{@code 2} - 支付宝。
     */
    private Integer payType;

    /**
     * 订单状态：{@code 0} - 待支付，{@code 1} - 已支付，{@code 2} - 已过期。
     */
    private Integer status;

    /**
     * 支付宝交易号，支付成功后由支付宝回调返回。
     */
    private String tradeNo;

    /**
     * 支付成功时间。
     */
    private LocalDateTime paidAt;

    /**
     * 订单过期时间，创建后15分钟未支付则自动过期。
     */
    private LocalDateTime expireAt;
}
