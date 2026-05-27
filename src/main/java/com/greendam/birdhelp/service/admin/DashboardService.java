package com.greendam.birdhelp.service.admin;

import com.greendam.birdhelp.model.vo.admin.DashboardVO;

/**
 * 仪表盘数据服务接口。
 * <p>
 * 提供管理后台首页仪表盘所需的各种统计数据，包括用户数量、项目数量、
 * 文件数量以及今日新增数据等运营指标。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数据。
     * <p>
     * 统计信息包括：
     * </p>
     * <ul>
     *   <li>用户总数和今日新增用户数</li>
     *   <li>项目总数</li>
     *   <li>文件总数</li>
     *   <li>今日生成任务数</li>
     *   <li>各等级用户分布情况</li>
     * </ul>
     *
     * @return 仪表盘统计数据视图对象
     */
    DashboardVO getStats();
}
