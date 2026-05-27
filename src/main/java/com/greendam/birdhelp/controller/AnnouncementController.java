package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.vo.admin.AnnouncementVO;
import com.greendam.birdhelp.service.admin.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 公告接口控制器（用户端），提供面向普通用户的公告查询接口。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>查询当前已发布（启用的）公告列表，供用户端展示</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    /**
     * <p>获取当前已启用的公告列表。</p>
     *
     * <p>仅返回状态为已发布且在当前时间范围内的公告，按发布时间降序排列。</p>
     *
     * @return 已启用的公告视图对象列表
     */
    @GetMapping("/active")
    public BaseResponse<List<AnnouncementVO>> active() {
        return BaseResponse.success(announcementService.getActiveAnnouncements());
    }
}
