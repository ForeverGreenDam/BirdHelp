package com.greendam.birdhelp.controller.admin;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.vo.admin.DashboardVO;
import com.greendam.birdhelp.service.admin.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 管理员仪表盘接口控制器，提供后台首页的统计数据展示。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>统计概览：获取用户总数、项目总数、文件总数等关键指标</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private DashboardService dashboardService;

    /**
     * <p>获取后台仪表盘统计数据。</p>
     *
     * @return 包含用户数、项目数、文件数等关键指标的视图对象
     */
    @GetMapping("/stats")
    public BaseResponse<DashboardVO> stats() {
        return BaseResponse.success(dashboardService.getStats());
    }
}
