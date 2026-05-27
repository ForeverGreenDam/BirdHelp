package com.greendam.birdhelp.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * API 密钥实体，映射 {@code api_key} 表。
 * </p>
 *
 * <h3>用途</h3>
 * <p>存储 AI 供应商的 API 密钥、模型信息及连接配置，用于系统调用大语言模型时的认证与路由。</p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 主键 ID</li>
 *   <li>{@code providerName} - 供应商名称，例如 {@code "OpenAI"}、{@code "Anthropic"}</li>
 *   <li>{@code apiKey} - API 密钥原文</li>
 *   <li>{@code baseUrl} - 供应商 API 的基础地址</li>
 *   <li>{@code modelName} - 模型名称，例如 {@code "gpt-4o"}、{@code "claude-3-opus"}</li>
 *   <li>{@code modelType} - 模型类型分类，如 {@code "text"}、{@code "image"}</li>
 *   <li>{@code enabled} - 启用状态：{@code 1} - 启用，{@code 0} - 禁用</li>
 *   <li>{@code description} - 密钥备注说明</li>
 * </ul>
 *
 * @author ForeverGreenDam
 * @see com.greendam.birdhelp.model.entity.BaseEntity
 */
@EqualsAndHashCode(callSuper = false)
@TableName(value = "api_key")
@Data
public class ApiKey extends BaseEntity {

    /**
     * 主键 ID。
     */
    @TableId
    private Long id;

    /**
     * 供应商名称，例如 {@code "OpenAI"}、{@code "Anthropic"}。
     */
    private String providerName;

    /**
     * API 密钥原文。
     */
    private String apiKey;

    /**
     * 供应商 API 的基础地址。
     */
    private String baseUrl;

    /**
     * 模型名称，例如 {@code "gpt-4o"}、{@code "claude-3-opus"}。
     */
    private String modelName;

    /**
     * 模型类型分类，如 {@code "text"}、{@code "image"}。
     */
    private String modelType;

    /**
     * 启用状态：{@code 1} - 启用，{@code 0} - 禁用。
     */
    private Integer enabled;

    /**
     * 密钥备注说明。
     */
    private String description;
}
