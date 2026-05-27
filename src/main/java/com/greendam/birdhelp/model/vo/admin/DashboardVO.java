package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * <p>
 * 仪表盘视图对象，用于展示后台首页的统计数据概览。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code totalUsers} - 用户总数</li>
 *   <li>{@code todayNewUsers} - 今日新增用户数</li>
 *   <li>{@code totalProjects} - 项目总数</li>
 *   <li>{@code totalFiles} - 文件总数</li>
 *   <li>{@code todayGenerationTasks} - 今日生成的文档任务数</li>
 *   <li>{@code userCountByLevel} - 按会员等级统计的用户数量映射，Key 为会员等级，Value 为用户数</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class DashboardVO {
    /**
     * 用户总数。
     */
    private Long totalUsers;
    /**
     * 今日新增用户数。
     */
    private Long todayNewUsers;
    /**
     * 项目总数。
     */
    private Long totalProjects;
    /**
     * 文件总数。
     */
    private Long totalFiles;
    /**
     * 今日生成的文档任务数。
     */
    private Long todayGenerationTasks;
    /**
     * 按会员等级统计的用户数量映射，Key 为会员等级，Value 为用户数。
     */
    private Map<Integer, Long> userCountByLevel;
}
