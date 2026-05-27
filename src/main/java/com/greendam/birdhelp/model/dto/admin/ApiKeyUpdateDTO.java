package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * API 密钥更新 DTO，用于接收管理员修改已有 API 密钥的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - 要更新的 API 密钥 ID，不能为空</li>
 *   <li>{@code providerName} - 供应商名称，例如 {@code "OpenAI"}、{@code "Anthropic"}</li>
 *   <li>{@code apiKey} - API 密钥原文</li>
 *   <li>{@code baseUrl} - 供应商 API 的基础地址</li>
 *   <li>{@code modelName} - 模型名称，例如 {@code "gpt-4o"}、{@code "claude-3-opus"}</li>
 *   <li>{@code modelType} - 模型类型分类，如 {@code "text"}、{@code "image"}</li>
 *   <li>{@code description} - 密钥备注说明</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class ApiKeyUpdateDTO {
    /**
     * 要更新的 API 密钥 ID，不能为空。
     */
    @NotNull(message = "ID不能为空")
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
     * 密钥备注说明。
     */
    private String description;
}
