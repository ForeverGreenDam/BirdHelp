package com.greendam.birdhelp.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.greendam.birdhelp.mapper.FileRecordMapper;
import com.greendam.birdhelp.mapper.ProjectMapper;
import com.greendam.birdhelp.mapper.QuotaLogMapper;
import com.greendam.birdhelp.mapper.SysUserMapper;
import com.greendam.birdhelp.model.entity.SysUser;
import com.greendam.birdhelp.model.entity.UserQuota;
import com.greendam.birdhelp.model.vo.admin.DashboardVO;
import com.greendam.birdhelp.service.admin.DashboardService;
import com.greendam.birdhelp.service.impl.QuotaServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘数据服务实现类。
 * <p>
 * 通过聚合查询用户、项目、文件记录和配额日志等数据，提供管理后台首页
 * 仪表盘所需的各项运营统计数据。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private FileRecordMapper fileRecordMapper;

    @Resource
    private QuotaLogMapper quotaLogMapper;

    @Resource
    private QuotaServiceImpl quotaServiceImpl;

    @Override
    public DashboardVO getStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long totalUsers = sysUserMapper.selectCount(null);
        long todayNewUsers = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().ge(SysUser::getCreateTime, todayStart));
        long totalProjects = projectMapper.selectCount(null);
        long totalFiles = fileRecordMapper.selectCount(null);

        long todayGenerationTasks = quotaLogMapper.selectCount(
                new LambdaQueryWrapper<com.greendam.birdhelp.model.entity.QuotaLog>()
                        .eq(com.greendam.birdhelp.model.entity.QuotaLog::getChangeType, 1)
                        .ge(com.greendam.birdhelp.model.entity.QuotaLog::getCreateTime, todayStart));

        Map<Integer, Long> userCountByLevel = new HashMap<>();
        List<UserQuota> allQuotas = quotaServiceImpl.list();
        Map<Integer, Long> levelCounts = new HashMap<>();
        for (UserQuota q : allQuotas) {
            levelCounts.merge(q.getMemberLevel(), 1L, Long::sum);
        }
        userCountByLevel.putAll(levelCounts);

        return DashboardVO.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .totalProjects(totalProjects)
                .totalFiles(totalFiles)
                .todayGenerationTasks(todayGenerationTasks)
                .userCountByLevel(userCountByLevel)
                .build();
    }
}
