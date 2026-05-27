package com.greendam.birdhelp.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.mapper.OperationLogMapper;
import com.greendam.birdhelp.model.entity.OperationLog;
import com.greendam.birdhelp.model.vo.admin.OperationLogVO;
import com.greendam.birdhelp.service.admin.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志管理服务实现类。
 * <p>
 * 实现管理员操作日志的记录与分页查询功能。日志记录包括操作人、操作类型、
 * 操作目标等信息，支持按多个维度进行组合条件查询。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    @Override
    public void record(Long adminId, String adminName, String action, String targetType,
                       String targetId, String detail) {
        OperationLog logEntry = new OperationLog();
        logEntry.setAdminId(adminId);
        logEntry.setAdminName(adminName);
        logEntry.setAction(action);
        logEntry.setTargetType(targetType);
        logEntry.setTargetId(targetId);
        logEntry.setDetail(detail);
        save(logEntry);
    }

    @Override
    public Page<OperationLogVO> listLogs(int page, int size, Long adminId, String action,
                                         String targetType, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(adminId != null, OperationLog::getAdminId, adminId)
                .eq(action != null, OperationLog::getAction, action)
                .eq(targetType != null, OperationLog::getTargetType, targetType)
                .ge(startTime != null, OperationLog::getCreateTime, startTime)
                .le(endTime != null, OperationLog::getCreateTime, endTime)
                .orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> logPage = page(Page.of(page, size), wrapper);
        Page<OperationLogVO> voPage = new Page<>(page, size, logPage.getTotal());
        voPage.setRecords(logPage.getRecords().stream().map(l -> OperationLogVO.builder()
                .id(l.getId())
                .adminId(l.getAdminId())
                .adminName(l.getAdminName())
                .action(l.getAction())
                .targetType(l.getTargetType())
                .targetId(l.getTargetId())
                .detail(l.getDetail())
                .createTime(l.getCreateTime())
                .build()).toList());
        return voPage;
    }
}
