package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.vo.admin.OperationLogVO;
import com.greendam.birdhelp.service.admin.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员操作日志接口控制器，提供管理员操作日志的分页查询功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>分页查询操作日志，支持按管理员 ID、操作类型、目标类型及时间范围筛选</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/operation-log")
public class AdminOperationLogController {

    @Resource
    private OperationLogService operationLogService;

    /**
     * <p>分页查询操作日志列表。</p>
     *
     * @param page       页码，默认为 1
     * @param size       每页条数，默认为 10
     * @param adminId    管理员 ID（可选）
     * @param action     操作动作（可选），如创建、更新、删除等
     * @param targetType 操作目标类型（可选），如项目、用户、密钥等
     * @param startTime  起始时间（可选），ISO 日期时间格式
     * @param endTime    截止时间（可选），ISO 日期时间格式
     * @return 操作日志分页数据
     */
    @GetMapping("/list")
    public BaseResponse<Page<OperationLogVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return BaseResponse.success(operationLogService.listLogs(page, size, adminId, action, targetType, startTime, endTime));
    }
}
