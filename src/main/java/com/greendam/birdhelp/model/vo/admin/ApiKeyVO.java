package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * API 密钥视图对象，用于展示 API 密钥的列表及详情信息。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code id} - API 密钥 ID</li>
 *   <li>{@code providerName} - 供应商名称，例如 {@code "OpenAI"}、{@code "Anthropic"}</li>
 *   <li>{@code apiKeyMasked} - 脱敏后的 API 密钥（仅显示前后几位）</li>
 *   <li>{@code baseUrl} - 供应商 API 的基础地址</li>
 *   <li>{@code modelName} - 模型名称，例如 {@code "gpt-4o"}、{@code "claude-3-opus"}</li>
 *   <li>{@code enabled} - 启用状态：{@code 1} - 启用，{@code 0} - 禁用</li>
 *   <li>{@code description} - 密钥备注说明</li>
 *   <li>{@code createTime} - 创建时间</li>
 *   <li>{@code updateTime} - 更新时间</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class ApiKeyVO {
    /**
     * API 密钥 ID。
     */
    private Long id;
    /**
     * 供应商名称，例如 {@code "OpenAI"}、{@code "Anthropic"}。
     */
    private String providerName;
    /**
     * 脱敏后的 API 密钥（仅显示前后几位）。
     */
    private String apiKeyMasked;
    /**
     * 供应商 API 的基础地址。
     */
    private String baseUrl;
    /**
     * 模型名称，例如 {@code "gpt-4o"}、{@code "claude-3-opus"}。
     */
    private String modelName;
    /**
     * 启用状态：{@code 1} - 启用，{@code 0} - 禁用。
     */
    private Integer enabled;
    /**
     * 密钥备注说明。
     */
    private String description;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
