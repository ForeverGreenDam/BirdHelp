package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * <p>
 * 会员套餐表实体类，映射数据表 {@code member_plan}。
 * 定义各会员套餐的价格、有效期、每日生成次数上限等配置信息。
 * </p>
 *
 * <h3>套餐等级</h3>
 * <ul>
 *   <li>{@code 1} - 月卡（30天有效）</li>
 *   <li>{@code 2} - 季卡（90天有效）</li>
 *   <li>{@code 3} - 年卡（365天有效）</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "member_plan")
@Data
public class MemberPlan extends BaseEntity {

    /**
     * 主键 ID，数据库自增。
     */
    @TableId
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

    /**
     * 上架状态：{@code 0} - 下架，{@code 1} - 上架。
     */
    private Integer status;
}
