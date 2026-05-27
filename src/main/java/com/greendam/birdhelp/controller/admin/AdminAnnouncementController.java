package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.dto.admin.AnnouncementCreateDTO;
import com.greendam.birdhelp.model.dto.admin.AnnouncementUpdateDTO;
import com.greendam.birdhelp.model.vo.admin.AnnouncementVO;
import com.greendam.birdhelp.service.admin.AnnouncementService;
import com.greendam.birdhelp.service.admin.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 管理员公告管理接口控制器，提供公告的增删改查功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>公告列表：分页查询公告，支持按状态筛选</li>
 *   <li>公告详情：查看指定公告的完整内容</li>
 *   <li>公告管理：新增、编辑、删除公告</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/announcement")
public class AdminAnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @Resource
    private OperationLogService operationLogService;

    /**
     * <p>分页查询公告列表。</p>
     *
     * @param page   页码，默认为 1
     * @param size   每页条数，默认为 10
     * @param status 公告状态（可选），如发布、草稿等
     * @return 公告分页数据
     */
    @GetMapping("/list")
    public BaseResponse<Page<AnnouncementVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return BaseResponse.success(announcementService.adminListAnnouncements(page, size, status));
    }

    /**
     * <p>查询指定公告的详细信息。</p>
     *
     * @param id 公告 ID
     * @return 公告详情视图对象
     */
    @GetMapping("/{id}")
    public BaseResponse<AnnouncementVO> detail(@PathVariable Long id) {
        return BaseResponse.success(announcementService.adminGetAnnouncement(id));
    }

    /**
     * <p>新增公告。</p>
     *
     * @param dto 包含公告标题、内容、状态等信息的请求体
     * @return 操作成功无数据返回
     */
    @PostMapping
    public BaseResponse<Void> create(@Valid @RequestBody AnnouncementCreateDTO dto) {
        announcementService.adminCreateAnnouncement(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "CREATE", "announcement", "", "创建公告: " + dto.getTitle());
        return BaseResponse.success();
    }

    /**
     * <p>更新公告信息。</p>
     *
     * @param dto 包含待更新字段的请求体
     * @return 操作成功无数据返回
     */
    @PutMapping
    public BaseResponse<Void> update(@Valid @RequestBody AnnouncementUpdateDTO dto) {
        announcementService.adminUpdateAnnouncement(dto);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "UPDATE", "announcement", dto.getId().toString(), "更新公告");
        return BaseResponse.success();
    }

    /**
     * <p>删除指定公告。</p>
     *
     * @param id 待删除的公告 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        announcementService.adminDeleteAnnouncement(id);
        operationLogService.record(BaseContext.getCurrentId(), BaseContext.getCurrentName(),
                "DELETE", "announcement", id.toString(), "删除公告");
        return BaseResponse.success();
    }
}
