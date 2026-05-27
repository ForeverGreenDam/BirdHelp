package com.greendam.birdhelp.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.model.dto.admin.AnnouncementCreateDTO;
import com.greendam.birdhelp.model.dto.admin.AnnouncementUpdateDTO;
import com.greendam.birdhelp.model.entity.Announcement;
import com.greendam.birdhelp.model.vo.admin.AnnouncementVO;

import java.util.List;

/**
 * 公告管理服务接口。
 * <p>
 * 提供系统公告的完整管理功能，包括公告的创建、查询、更新、删除以及
 * 获取当前有效公告等操作。支持按状态筛选和分页查询。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 分页查询公告列表（管理端）。
     * <p>
     * 支持按公告状态进行筛选，结果按创建时间降序排列。
     * </p>
     *
     * @param page   页码，从1开始
     * @param size   每页记录数
     * @param status 公告状态（可选，为空时不以此条件过滤）
     * @return 包含公告视图对象的分页结果
     */
    Page<AnnouncementVO> adminListAnnouncements(int page, int size, Integer status);

    /**
     * 获取指定公告的详细信息（管理端）。
     * <p>
     * 如果指定的公告不存在，将抛出业务异常。
     * </p>
     *
     * @param id 公告ID
     * @return 公告视图对象
     * @throws BusinessException 如果指定的公告不存在
     */
    AnnouncementVO adminGetAnnouncement(Long id);

    /**
     * 创建新的系统公告。
     * <p>
     * 创建公告时可以设置标题、内容、状态和发布时间。
     * </p>
     *
     * @param dto 创建公告的请求数据
     */
    void adminCreateAnnouncement(AnnouncementCreateDTO dto);

    /**
     * 更新指定的公告信息。
     * <p>
     * 采用部分更新策略，仅更新传入的非空字段。如果指定的公告不存在，将抛出业务异常。
     * </p>
     *
     * @param dto 更新公告的请求数据，需包含待更新公告的ID
     * @throws BusinessException 如果指定的公告不存在
     */
    void adminUpdateAnnouncement(AnnouncementUpdateDTO dto);

    /**
     * 删除指定的公告。
     *
     * @param id 要删除的公告ID
     */
    void adminDeleteAnnouncement(Long id);

    /**
     * 获取当前所有有效的公告列表。
     * <p>
     * 查询条件为状态为已发布（{@code status = 1}）且发布时间不晚于当前时间，
     * 结果按发布时间降序排列。该方法通常用于前端用户端展示。
     * </p>
     *
     * @return 有效公告视图对象列表
     */
    List<AnnouncementVO> getActiveAnnouncements();
}
