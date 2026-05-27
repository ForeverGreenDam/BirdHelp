package com.greendam.birdhelp.model.dto.admin;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * API 密钥创建 DTO，用于接收管理员新增 API 密钥的请求参数。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code providerName} - 供应商名称，不能为空。例如 {@code "OpenAI"}、{@code "Anthropic"}</li>
 *   <li>{@code apiKey} - API 密钥原文，不能为空</li>
 *   <li>{@code baseUrl} - 供应商 API 的基础地址，不能为空</li>
 *   <li>{@code modelName} - 模型名称，例如 {@code "gpt-4o"}、{@code "claude-3-opus"}</li>
 *   <li>{@code description} - 密钥备注说明</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class ApiKeyCreateDTO {
    /**
     * 供应商名称，不能为空。例如 {@code "OpenAI"}、{@code "Anthropic"}。
     */
    @NotBlank(message = "供应商名称不能为空")
    private String providerName;

    /**
     * API 密钥原文，不能为空。
     */
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;

    /**
     * 供应商 API 的基础地址，不能为空。
     */
    @NotBlank(message = "Base URL不能为空")
    private String baseUrl;

    /**
     * 模型名称，例如 {@code "gpt-4o"}、{@code "claude-3-opus"}。
     */
    private String modelName;

    /**
     * 密钥备注说明。
     */
    private String description;
}
