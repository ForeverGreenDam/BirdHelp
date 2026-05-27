package com.greendam.birdhelp.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.model.dto.admin.ApiKeyCreateDTO;
import com.greendam.birdhelp.model.dto.admin.ApiKeyUpdateDTO;
import com.greendam.birdhelp.model.entity.ApiKey;
import com.greendam.birdhelp.model.vo.admin.ApiKeyVO;

import java.util.List;
import java.util.Map;

/**
 * API密钥管理服务接口。
 * <p>
 * 提供对第三方AI平台API密钥的完整生命周期管理，包括密钥的创建、查询、更新、删除、
 * 启用/禁用状态切换以及解密获取等功能。密钥在存储时使用AES加密，返回给前端时进行
 * 脱敏处理，确保敏感信息的安全。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface ApiKeyService extends IService<ApiKey> {

    /**
     * 分页查询API密钥列表。
     * <p>
     * 返回的密钥信息中，密钥字段会进行脱敏处理（仅显示前后4位），
     * 确保敏感信息不会完全暴露。结果按创建时间降序排列。
     * </p>
     *
     * @param page 页码，从1开始
     * @param size 每页记录数
     * @return 包含API密钥视图对象的分页结果
     */
    Page<ApiKeyVO> listApiKeys(int page, int size);

    /**
     * 获取指定API密钥的详细信息。
     * <p>
     * 返回的密钥信息中，密钥字段会进行脱敏处理（仅显示前后4位）。
     * 如果指定的密钥不存在，将抛出业务异常。
     * </p>
     *
     * @param id API密钥ID
     * @return API密钥视图对象
     * @throws BusinessException 如果指定的密钥不存在
     */
    ApiKeyVO getApiKeyDetail(Long id);

    /**
     * 创建新的API密钥。
     * <p>
     * 创建时会对密钥原文进行AES加密后存储到数据库，确保密钥明文不会
     * 以任何形式持久化。同时记录创建日志。
     * </p>
     *
     * @param dto 创建API密钥的请求数据，包含提供商名称、密钥原文、基础URL、模型名称等信息
     */
    void createApiKey(ApiKeyCreateDTO dto);

    /**
     * 更新指定的API密钥信息。
     * <p>
     * 采用部分更新策略，仅更新传入的非空字段。如果密钥原文发生变化，
     * 会自动重新加密后存储。如果指定的密钥不存在，将抛出业务异常。
     * </p>
     *
     * @param dto 更新API密钥的请求数据，需包含待更新密钥的ID
     * @throws BusinessException 如果指定的密钥不存在
     */
    void updateApiKey(ApiKeyUpdateDTO dto);

    /**
     * 删除指定的API密钥。
     * <p>
     * 直接从数据库中移除指定的密钥记录并记录删除日志。
     * </p>
     *
     * @param id 要删除的API密钥ID
     */
    void deleteApiKey(Long id);

    /**
     * 切换API密钥的启用/禁用状态。
     * <p>
     * 禁用的密钥将无法用于AI服务调用，可用于临时停用某个密钥而无需删除。
     * 如果指定的密钥不存在，将抛出业务异常。
     * </p>
     *
     * @param id      API密钥ID
     * @param enabled 目标状态，{@code true} 为启用，{@code false} 为禁用
     * @throws BusinessException 如果指定的密钥不存在
     */
    void toggleEnabled(Long id, Boolean enabled);

    /**
     * 根据提供商名称查询已解密的密钥列表。
     * <p>
     * 该方法仅返回已启用（{@code enabled = 1}）的密钥，并且返回的
     * {@code apiKey} 字段为解密后的明文，供AI服务调用时直接使用。
     * 参数支持为空，为空时不作为过滤条件。
     * </p>
     *
     * @param providerName 提供商名称（可选，为空或空字符串时不以此条件过滤）
     * @return 包含已解密密钥信息的列表，每项包含 providerName、apiKey、baseUrl、modelName 字段
     */
    List<Map<String, Object>> fetchDecryptedKeys(String providerName);

    /**
     * <p>根据模型名称解析 LLM 调用凭证。</p>
     *
     * <p>若指定 modelName，则查找匹配该名称且已启用的密钥；若未指定或未找到，则返回第一个已启用的密钥。
     * 返回的 Map 包含 apiKey（明文）、baseUrl、modelName 三个字段。</p>
     *
     * @param modelName 模型名称（可选，为 null 或空字符串时使用第一个可用密钥）
     * @return 包含 {@code apiKey}、{@code baseUrl}、{@code modelName} 的凭证 Map
     * @throws BusinessException 如果没有已启用的密钥
     */
    Map<String, String> resolveCredentials(String modelName);

    /**
     * <p>获取当前可用的模型列表，供前端下拉框选择。</p>
     *
     * <p>仅返回已启用（{@code enabled = 1}）的模型，不包含 API Key 等敏感信息。</p>
     *
     * @return 模型列表，每项包含 {@code modelName}、{@code providerName}、{@code description}
     */
    List<Map<String, String>> listAvailableModels();
}
