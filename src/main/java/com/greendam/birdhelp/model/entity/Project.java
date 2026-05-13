package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 项目表实体类，映射数据表 {@code project}。
 * </p>
 *
 * <h3>项目状态</h3>
 * <ul>
 *   <li>{@code status = 1}：活跃，正常使用中</li>
 *   <li>{@code status = 0}：已归档，隐藏但可重新激活</li>
 * </ul>
 *
 * <h3>文件计数</h3>
 * <p>{@code file_count} 为冗余缓存字段，上传文件时 +1，删除文件时 -1，
 * 避免每次列表查询时 COUNT 子查询。</p>
 *
 * @author ForeverGreenDam
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "project")
@Data
public class Project extends BaseEntity {

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
     * 项目名称。
     */
    private String name;

    /**
     * 项目描述。
     */
    private String description;

    /**
     * 状态：{@code 1} - 活跃，{@code 0} - 已归档。
     */
    private Integer status;

    /**
     * 项目下文件数量（冗余缓存）。
     */
    private Integer fileCount;
}
