package com.greendam.birdhelp.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.model.entity.OperationLog;
import com.greendam.birdhelp.model.vo.admin.OperationLogVO;

import java.time.LocalDateTime;

/**
 * 操作日志管理服务接口。
 * <p>
 * 提供管理员操作日志的记录和分页查询功能，用于审计和追踪管理员在后台管理系统中的
 * 各类操作行为，包括创建、更新、删除等操作的时间、操作人、操作对象和操作详情。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 记录一条操作日志。
     * <p>
     * 将管理员的操作行为持久化到数据库，包含操作人、操作类型、操作目标等信息，
     * 用于后续的审计追踪和操作回溯。
     * </p>
     *
     * @param adminId    执行操作的管理员ID
     * @param adminName  执行操作的管理员用户名
     * @param action     操作动作（如 CREATE、UPDATE、DELETE 等）
     * @param targetType 操作目标类型（如 "ApiKey"、"Announcement" 等）
     * @param targetId   操作目标的ID标识
     * @param detail     操作详情描述
     */
    void record(Long adminId, String adminName, String action, String targetType, String targetId, String detail);

    /**
     * 分页查询操作日志列表。
     * <p>
     * 支持按管理员ID、操作动作、操作目标类型以及操作时间范围进行过滤筛选。
     * 结果按创建时间降序排列，便于查看最新的操作记录。
     * </p>
     *
     * @param page       页码，从1开始
     * @param size       每页记录数
     * @param adminId    管理员ID（可选，为空时不以此条件过滤）
     * @param action     操作动作（可选，为空时不以此条件过滤）
     * @param targetType 操作目标类型（可选，为空时不以此条件过滤）
     * @param startTime  查询范围的起始时间（可选，为空时不以此条件过滤）
     * @param endTime    查询范围的结束时间（可选，为空时不以此条件过滤）
     * @return 包含操作日志视图对象的分页结果
     */
    Page<OperationLogVO> listLogs(int page, int size, Long adminId, String action, String targetType,
                                  LocalDateTime startTime, LocalDateTime endTime);
}
