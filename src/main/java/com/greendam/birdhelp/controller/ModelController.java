package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.service.admin.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 可用模型查询控制器，供前端生成页面展示可选的大语言模型列表。
 * </p>
 *
 * <h3>说明</h3>
 * <p>仅返回已启用的模型名称、供应商和描述，不暴露 API Key 等敏感信息。
 * 仅需携带普通用户 JWT Token 即可访问。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/model")
public class ModelController {

    @Resource
    private ApiKeyService apiKeyService;

    /**
     * <p>获取当前可用的 LLM 模型列表。</p>
     *
     * @return 模型列表，每项包含 {@code modelName}、{@code providerName}、{@code description}
     */
    @GetMapping("/list")
    public BaseResponse<List<Map<String, String>>> list() {
        return BaseResponse.success(apiKeyService.listAvailableModels());
    }
}
