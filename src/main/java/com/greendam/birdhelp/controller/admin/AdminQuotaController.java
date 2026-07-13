package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.dto.admin.QuotaConfigCreateDTO;
import com.greendam.birdhelp.model.dto.admin.QuotaConfigUpdateDTO;
import com.greendam.birdhelp.model.dto.admin.UserQuotaMemberUpdateDTO;
import com.greendam.birdhelp.model.entity.QuotaConfig;
import com.greendam.birdhelp.model.vo.admin.AdminQuotaLogVO;
import com.greendam.birdhelp.model.vo.admin.AdminUserQuotaVO;
import com.greendam.birdhelp.service.QuotaService;
import com.greendam.birdhelp.service.admin.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 管理员额度管理接口控制器，提供额度配置、用户额度调整及额度变更日志查询等功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>额度配置管理：查询和更新全局额度配置项</li>
 *   <li>用户额度管理：分页查询用户额度、手动调整额度、变更会员等级</li>
 *   <li>额度日志查询：分页查询额度变更历史，支持多条件筛选</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/quota")
public class AdminQuotaController {

    @Resource
    private QuotaService quotaService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * <p>查询所有额度配置项列表。</p>
     *
     * @return 额度配置项列表
     */
    @GetMapping("/config/list")
    public BaseResponse<List<QuotaConfig>> listConfigs() {
        return BaseResponse.success(quotaService.adminListConfigs());
    }

    /**
     * <p>更新额度配置项。</p>
     *
     * @param dto 包含配置项标识及更新值的请求体
     * @return 操作成功无数据返回
     */
    @PutMapping("/config")
    public BaseResponse<Void> updateConfig(@Valid @RequestBody QuotaConfigUpdateDTO dto) {
        quotaService.adminUpdateConfig(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "UPDATE", "quota_config", dto.getId().toString(), "更新额度配置");
        return BaseResponse.success();
    }

    /**
     * <p>新增额度配置。</p>
     *
     * @param dto 包含等级、每日上限及有效天数的请求体
     * @return 操作成功无数据返回
     */
    @PostMapping("/config")
    public BaseResponse<Void> createConfig(@Valid @RequestBody QuotaConfigCreateDTO dto) {
        quotaService.adminCreateConfig(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "CREATE", "quota_config", null, "新增额度配置，等级: " + dto.getLevel());
        return BaseResponse.success();
    }

    /**
     * <p>删除额度配置。</p>
     *
     * @param id 配置 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/config/{id}")
    public BaseResponse<Void> deleteConfig(@PathVariable Long id) {
        quotaService.adminDeleteConfig(id);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "DELETE", "quota_config", id.toString(), "删除额度配置");
        return BaseResponse.success();
    }

    /**
     * <p>分页查询用户额度列表。</p>
     *
     * <p>支持按用户 ID 和会员等级进行筛选。</p>
     *
     * @param page        页码，默认为 1
     * @param size        每页条数，默认为 10
     * @param userId      用户 ID（可选），按用户精确筛选
     * @param memberLevel 会员等级（可选），按会员等级筛选
     * @return 用户额度分页数据
     */
    @GetMapping("/user/list")
    public BaseResponse<Page<AdminUserQuotaVO>> listUserQuotas(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer memberLevel) {
        return BaseResponse.success(quotaService.adminListUserQuotas(page, size, userId, memberLevel));
    }

    /**
     * <p>变更指定用户的会员等级。</p>
     *
     * <p>会员等级变更后，该用户的可用额度将根据新等级对应的配置重新计算。</p>
     *
     * @param dto 包含用户 ID 及目标会员等级的请求体
     * @return 操作成功无数据返回
     */
    @PutMapping("/user/member")
    public BaseResponse<Void> changeMemberLevel(@Valid @RequestBody UserQuotaMemberUpdateDTO dto) {
        quotaService.adminChangeMemberLevel(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "UPDATE", "user_quota", dto.getUserId().toString(), "变更会员等级");
        return BaseResponse.success();
    }

    /**
     * <p>分页查询额度变更日志。</p>
     *
     * <p>支持按用户 ID、变更类型和时间范围进行筛选。</p>
     *
     * @param page       页码，默认为 1
     * @param size       每页条数，默认为 10
     * @param userId     用户 ID（可选）
     * @param changeType 变更类型（可选），如管理员调整、消费扣除等
     * @param startTime  起始时间（可选），ISO 日期时间格式
     * @param endTime    截止时间（可选），ISO 日期时间格式
     * @return 额度变更日志分页数据
     */
    @GetMapping("/log/list")
    public BaseResponse<Page<AdminQuotaLogVO>> listLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer changeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return BaseResponse.success(quotaService.adminListQuotaLogs(page, size, userId, changeType, startTime, endTime));
    }
}
