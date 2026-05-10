package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 额度流水表实体类，映射数据表 {@code quota_log}。
 * 记录每次额度扣减/退还操作，用于对账追溯。
 * </p>
 *
 * <h3>变更类型</h3>
 * <ul>
 *   <li>{@code changeType = 1}：扣减（AI 生成任务提交时）</li>
 *   <li>{@code changeType = 2}：退还（生成失败时）</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "quota_log")
@Data
public class QuotaLog extends BaseEntity {

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
     * 变更类型：{@code 1} - 扣减，{@code 2} - 退还。
     */
    private Integer changeType;

    /**
     * 关联业务 ID（如生成任务 ID），可为空。
     */
    private Long relatedId;
}
