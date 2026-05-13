package com.greendam.birdhelp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.model.entity.Project;
import com.greendam.birdhelp.model.vo.ProjectVO;

/**
 * <p>
 * 项目服务接口，继承 MyBatis-Plus {@link IService} 获得通用 CRUD 能力。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface ProjectService extends IService<Project> {

    /**
     * 创建项目。
     *
     * @param name        项目名称
     * @param description 项目描述
     * @param userId      用户 ID
     * @return 项目视图
     */
    ProjectVO createProject(String name, String description, Long userId);

    /**
     * 删除项目（级联软删除文件，通知 AI 模块清理向量）。
     *
     * @param id     项目 ID
     * @param userId 用户 ID
     */
    void deleteProject(Long id, Long userId);

    /**
     * 编辑项目信息。
     *
     * @param id          项目 ID
     * @param name        新名称（为 null 则不更新）
     * @param description 新描述（为 null 则不更新）
     * @param userId      用户 ID
     * @return 更新后的项目视图
     */
    ProjectVO updateProject(Long id, String name, String description, Long userId);

    /**
     * 查询项目详情。
     *
     * @param id     项目 ID
     * @param userId 用户 ID
     * @return 项目视图
     */
    ProjectVO getProject(Long id, Long userId);

    /**
     * 分页查询用户项目列表，按更新时间倒序。
     *
     * @param page   页码
     * @param size   每页条数
     * @param userId 用户 ID
     * @return 分页结果
     */
    Page<ProjectVO> listProjects(int page, int size, Long userId);

    /**
     * 归档项目。
     *
     * @param id     项目 ID
     * @param userId 用户 ID
     */
    void archiveProject(Long id, Long userId);

    /**
     * 激活已归档项目。
     *
     * @param id     项目 ID
     * @param userId 用户 ID
     */
    void activateProject(Long id, Long userId);

    /**
     * 递增项目文件计数（上传文件时调用）。
     *
     * @param projectId 项目 ID
     */
    void incrementFileCount(Long projectId);

    /**
     * 递减项目文件计数（删除文件时调用）。
     *
     * @param projectId 项目 ID
     */
    void decrementFileCount(Long projectId);
}
