package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.dto.admin.ApiKeyCreateDTO;
import com.greendam.birdhelp.model.dto.admin.ApiKeyUpdateDTO;
import com.greendam.birdhelp.model.vo.admin.ApiKeyVO;
import com.greendam.birdhelp.service.admin.ApiKeyService;
import com.greendam.birdhelp.service.admin.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 管理员 API 密钥管理接口控制器，提供第三方 AI 平台 API 密钥的增删改查及启用/禁用功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>密钥列表查询：分页获取所有 API 密钥</li>
 *   <li>密钥详情：查看指定密钥的完整信息</li>
 *   <li>密钥管理：新增、编辑、删除 API 密钥</li>
 *   <li>状态切换：启用或禁用指定 API 密钥</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/api-key")
public class AdminApiKeyController {

    @Resource
    private ApiKeyService apiKeyService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * <p>分页查询 API 密钥列表。</p>
     *
     * @param page 页码，默认为 1
     * @param size 每页条数，默认为 10
     * @return API 密钥分页数据
     */
    @GetMapping("/list")
    public BaseResponse<Page<ApiKeyVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return BaseResponse.success(apiKeyService.listApiKeys(page, size));
    }

    /**
     * <p>查询指定 API 密钥的详细信息。</p>
     *
     * @param id 密钥 ID
     * @return API 密钥详情视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<ApiKeyVO> detail(@PathVariable Long id) {
        return BaseResponse.success(apiKeyService.getApiKeyDetail(id));
    }

    /**
     * <p>新增 API 密钥。</p>
     *
     * @param dto 包含提供商名称、模型类型、密钥内容等信息的请求体
     * @return 操作成功无数据返回
     */
    @PostMapping
    public BaseResponse<Void> create(@Valid @RequestBody ApiKeyCreateDTO dto) {
        apiKeyService.createApiKey(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "CREATE", "api_key", dto.getProviderName(), "创建API密钥");
        return BaseResponse.success();
    }

    /**
     * <p>更新 API 密钥信息。</p>
     *
     * @param dto 包含待更新字段的请求体
     * @return 操作成功无数据返回
     */
    @PutMapping
    public BaseResponse<Void> update(@Valid @RequestBody ApiKeyUpdateDTO dto) {
        apiKeyService.updateApiKey(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "UPDATE", "api_key", dto.getId().toString(), "更新API密钥");
        return BaseResponse.success();
    }

    /**
     * <p>删除指定 API 密钥。</p>
     *
     * @param id 待删除的密钥 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "DELETE", "api_key", id.toString(), "删除API密钥");
        return BaseResponse.success();
    }

    /**
     * <p>启用或禁用指定 API 密钥。</p>
     *
     * @param id      密钥 ID
     * @param enabled {@code true} 启用，{@code false} 禁用
     * @return 操作成功无数据返回
     */
    @PutMapping("/{id}/enabled")
    public BaseResponse<Void> toggleEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        apiKeyService.toggleEnabled(id, enabled);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "UPDATE", "api_key", id.toString(), enabled ? "启用API密钥" : "禁用API密钥");
        return BaseResponse.success();
    }
}
