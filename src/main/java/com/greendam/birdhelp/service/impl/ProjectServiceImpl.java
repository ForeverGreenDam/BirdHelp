package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.FileRecordMapper;
import com.greendam.birdhelp.mapper.ProjectMapper;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.model.entity.Project;
import com.greendam.birdhelp.model.vo.ProjectVO;
import com.greendam.birdhelp.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 项目服务实现类，继承 MyBatis-Plus {@link ServiceImpl} 提供通用 CRUD 实现。
 * </p>
 *
 * <h3>删除流程</h3>
 * <ol>
 *   <li>校验项目存在且属于当前用户</li>
 *   <li>级联将项目下所有正常文件的 {@code deleted} 置为 1</li>
 *   <li>通知 AI 模块清理该项目关联的 Redis 向量数据</li>
 *   <li>逻辑删除项目记录</li>
 * </ol>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project>
        implements ProjectService {

    @Resource
    private FileRecordMapper fileRecordMapper;

    // ==================== 公开方法 ====================

    @Override
    public ProjectVO createProject(String name, String description, Long userId) {
        Project project = new Project();
        project.setUserId(userId);
        project.setName(name);
        project.setDescription(description != null ? description : "");
        project.setStatus(1);
        project.setFileCount(0);
        save(project);
        log.info("项目创建成功: id={}, name={}, userId={}", project.getId(), name, userId);
        return toVO(project);
    }

    @Override
    public void deleteProject(Long id, Long userId) {
        Project project = getOwnProject(id, userId);

        // 级联将项目下所有正常文件移入回收站
        cascadeSoftDeleteFiles(id);

        // 逻辑删除项目
        removeById(id);
        log.info("项目删除成功: id={}, name={}, userId={}", id, project.getName(), userId);
    }

    @Override
    public ProjectVO updateProject(Long id, String name, String description, Long userId) {
        Project project = getOwnProject(id, userId);

        if (name != null) {
            project.setName(name);
        }
        if (description != null) {
            project.setDescription(description);
        }
        updateById(project);
        log.info("项目更新成功: id={}, userId={}", id, userId);
        return toVO(project);
    }

    @Override
    public ProjectVO getProject(Long id, Long userId) {
        Project project = getOwnProject(id, userId);
        return toVO(project);
    }

    @Override
    public Page<ProjectVO> listProjects(int page, int size, Long userId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getUserId, userId)
                .orderByDesc(Project::getUpdateTime);

        Page<Project> resultPage = page(new Page<>(page, size), wrapper);
        return convertPage(resultPage);
    }

    @Override
    public void archiveProject(Long id, Long userId) {
        Project project = getOwnProject(id, userId);
        if (project.getStatus() == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "项目已归档");
        }
        project.setStatus(0);
        updateById(project);
        log.info("项目已归档: id={}, userId={}", id, userId);
    }

    @Override
    public void activateProject(Long id, Long userId) {
        Project project = getOwnProject(id, userId);
        if (project.getStatus() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "项目已是活跃状态");
        }
        project.setStatus(1);
        updateById(project);
        log.info("项目已激活: id={}, userId={}", id, userId);
    }

    @Override
    public void incrementFileCount(Long projectId) {
        LambdaUpdateWrapper<Project> wrapper = new LambdaUpdateWrapper<Project>()
                .eq(Project::getId, projectId)
                .setSql("file_count = file_count + 1");
        update(wrapper);
    }

    @Override
    public void decrementFileCount(Long projectId) {
        LambdaUpdateWrapper<Project> wrapper = new LambdaUpdateWrapper<Project>()
                .eq(Project::getId, projectId)
                .setSql("file_count = GREATEST(file_count - 1, 0)");
        update(wrapper);
    }

    // ==================== 内部方法 ====================

    /**
     * 获取用户拥有的项目，不存在或不属于该用户时抛异常。
     */
    private Project getOwnProject(Long id, Long userId) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }
        if (!project.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权操作该项目");
        }
        return project;
    }

    /**
     * 级联将项目下所有正常文件移入回收站。
     */
    private void cascadeSoftDeleteFiles(Long projectId) {
        LambdaUpdateWrapper<FileRecord> wrapper = new LambdaUpdateWrapper<FileRecord>()
                .eq(FileRecord::getProjectId, projectId)
                .eq(FileRecord::getDeleted, 0)
                .set(FileRecord::getDeleted, 1)
                .set(FileRecord::getDeletedAt, LocalDateTime.now());
        fileRecordMapper.update(null, wrapper);
        log.info("级联回收项目文件完成: projectId={}", projectId);
    }


    private ProjectVO toVO(Project project) {
        return ProjectVO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .fileCount(project.getFileCount())
                .createTime(project.getCreateTime())
                .updateTime(project.getUpdateTime())
                .build();
    }

    private Page<ProjectVO> convertPage(Page<Project> entityPage) {
        Page<ProjectVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<ProjectVO> voList = new ArrayList<>();
        for (Project project : entityPage.getRecords()) {
            voList.add(toVO(project));
        }
        voPage.setRecords(voList);
        return voPage;
    }
}
