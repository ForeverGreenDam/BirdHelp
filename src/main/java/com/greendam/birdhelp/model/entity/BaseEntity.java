package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 审计字段基类。所有需要自动填充审计信息的实体类均应继承此类。
 * </p>
 *
 * <h3>自动填充</h3>
 * <p>字段通过 {@link com.greendam.birdhelp.handler.MyMetaObjectHandler} 自动填充：</p>
 * <ul>
 *   <li>{@code createTime} 插入时填入 {@link LocalDateTime#now()}</li>
 *   <li>{@code createBy} 插入时填入当前登录用户 ID（来自 {@link com.greendam.birdhelp.common.context.BaseContext}）</li>
 *   <li>{@code updateTime} 插入和更新时填入 {@link LocalDateTime#now()}</li>
 *   <li>{@code updateBy} 插入和更新时填入当前登录用户 ID</li>
 *   <li>{@code delFlag} 由 MyBatis-Plus 逻辑删除插件自动管理</li>
 * </ul>
 *
 * @author ForeverGreenDam
 * @see com.greendam.birdhelp.handler.MyMetaObjectHandler
 */
@Data
public class BaseEntity implements Serializable {

    /**
     * 创建时间，INSERT 时由 {@code MyMetaObjectHandler} 自动填充。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建人 ID（字符串形式），INSERT 时自动填充。
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 更新时间，INSERT 和 UPDATE 时自动填充。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 更新人 ID（字符串形式），INSERT 和 UPDATE 时自动填充。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 逻辑删除标记：{@code 0} - 未删除，{@code 1} - 已删除。由 MyBatis-Plus 逻辑删除插件自动处理。
     */
    @TableLogic
    private Integer delFlag;
}
