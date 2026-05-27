package com.greendam.birdhelp.internal;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.service.admin.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * API 密钥内部接口控制器，供 AI 模块内部调用以获取解密后的 API 密钥。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>密钥获取：根据提供商名称和模型类型筛选并返回解密后的 API 密钥列表</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <p>该接口不对外暴露，仅限内部服务间调用，用于 AI 文档生成模块获取所需平台的密钥凭据。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/internal/api-key")
public class ApiKeyInternalController {

    @Resource
    private ApiKeyService apiKeyService;

    /**
     * <p>获取解密后的 API 密钥列表。</p>
     *
     * <p>根据指定的提供商名称和模型类型进行筛选，返回密钥明文供 AI 模块调用。
     * 若参数为空则不进行对应维度的过滤。</p>
     *
     * @param providerName 提供商名称（可选），如 {@code deepseek}、{@code siliconflow} 等
     * @param modelType    模型类型（可选），如 {@code chat}、{@code image} 等
     * @return 解密后的 API 密钥列表，每个元素为包含密钥字段的键值映射
     */
    @PostMapping("/fetch")
    public BaseResponse<List<Map<String, Object>>> fetchKeys(
            @RequestParam(required = false) String providerName,
            @RequestParam(required = false) String modelType) {
        List<Map<String, Object>> keys = apiKeyService.fetchDecryptedKeys(providerName, modelType);
        log.info("内部接口: AI模块获取API密钥, providerName={}, modelType={}, 返回{}条", providerName, modelType, keys.size());
        return BaseResponse.success(keys);
    }
}
