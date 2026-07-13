package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户额度表实体类，映射数据表 {@code user_quota}。
 * 每个用户对应一条记录，记录当前会员等级、到期时间及当日使用情况。
 * </p>
 *
 * <h3>额度扣减策略</h3>
 * <ul>
 *   <li>每日额度上限由 {@link QuotaConfig} 决定，取免费额度和会员额度的最大值</li>
 *   <li>不存储"剩余次数"，实时计算 {@code 当日额度上限 - dailyUsed}</li>
 *   <li>{@code daily_date} 不等于今天时，表示已用次数已由定时任务重置</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "user_quota")
@Data
public class UserQuota extends BaseEntity {

    /**
     * 主键 ID，数据库自增。
     */
    @TableId
    private Long id;

    /**
     * 用户 ID，关联 {@code sys_user.id}。
     */
    private Long userId;

    /**
     * 会员等级：{@code 0} - 免费，{@code 1} - 月卡，{@code 2} - 季卡，{@code 3} - 年卡。
     */
    private Integer memberLevel;

    /**
     * 会员到期时间，过期后回退到免费等级。免费用户为 {@code null}。
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime memberExpireAt;

    /**
     * 今日已用次数，每日 0 点由定时任务重置为 0。
     */
    private Integer dailyUsed;

    /**
     * 已用次数对应的日期，用于判断是否需要跨天重置。
     */
    private LocalDate dailyDate;
}
