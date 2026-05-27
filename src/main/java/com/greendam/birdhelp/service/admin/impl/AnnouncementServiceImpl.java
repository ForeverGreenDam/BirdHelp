package com.greendam.birdhelp.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.AnnouncementMapper;
import com.greendam.birdhelp.model.dto.admin.AnnouncementCreateDTO;
import com.greendam.birdhelp.model.dto.admin.AnnouncementUpdateDTO;
import com.greendam.birdhelp.model.entity.Announcement;
import com.greendam.birdhelp.model.vo.admin.AnnouncementVO;
import com.greendam.birdhelp.service.admin.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告管理服务实现类。
 * <p>
 * 实现系统公告的完整管理功能，包括公告的创建、查询、更新、删除以及
 * 获取当前有效公告等操作。支持管理端的分页查询和状态筛选，同时为
 * 用户端提供活跃公告的查询接口。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Override
    public Page<AnnouncementVO> adminListAnnouncements(int page, int size, Integer status) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Announcement::getStatus, status)
                .orderByDesc(Announcement::getCreateTime);
        Page<Announcement> entityPage = page(Page.of(page, size), wrapper);
        Page<AnnouncementVO> voPage = new Page<>(page, size, entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public AnnouncementVO adminGetAnnouncement(Long id) {
        Announcement entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        }
        return toVO(entity);
    }

    @Override
    public void adminCreateAnnouncement(AnnouncementCreateDTO dto) {
        Announcement entity = new Announcement();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        save(entity);
        log.info("创建公告: title={}", dto.getTitle());
    }

    @Override
    public void adminUpdateAnnouncement(AnnouncementUpdateDTO dto) {
        Announcement entity = getById(dto.getId());
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "公告不存在");
        }
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getContent() != null) entity.setContent(dto.getContent());
        if (dto.getStatus() != null) {
            if (dto.getStatus() == 1 && (entity.getStatus() == null || entity.getStatus() != 1)) {
                entity.setPublishTime(LocalDateTime.now());
            }
            entity.setStatus(dto.getStatus());
        }
        updateById(entity);
        log.info("更新公告: id={}", dto.getId());
    }

    @Override
    public void adminDeleteAnnouncement(Long id) {
        removeById(id);
        log.info("删除公告: id={}", id);
    }

    @Override
    public List<AnnouncementVO> getActiveAnnouncements() {
        List<Announcement> list = list(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .le(Announcement::getPublishTime, LocalDateTime.now())
                .orderByDesc(Announcement::getPublishTime));
        return list.stream().map(this::toVO).toList();
    }

    /**
     * 将公告实体对象转换为视图对象。
     * <p>
     * 使用建造者模式构建 {@link AnnouncementVO} 实例，
     * 复制实体中的所有字段到视图对象中。
     * </p>
     *
     * @param entity 公告实体对象
     * @return 公告视图对象
     */
    private AnnouncementVO toVO(Announcement entity) {
        return AnnouncementVO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .status(entity.getStatus())
                .publishTime(entity.getPublishTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }
}
