package com.greendam.birdhelp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.dto.CreateProjectDTO;
import com.greendam.birdhelp.model.dto.UpdateProjectDTO;
import com.greendam.birdhelp.model.vo.ProjectVO;
import com.greendam.birdhelp.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 项目模块接口控制器，提供项目 CRUD、归档/激活等功能。
 * </p>
 *
 * <h3>鉴权说明</h3>
 * <p>所有接口均需携带有效 JWT Token，仅允许操作本人的项目。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Resource
    private ProjectService projectService;

    /**
     * 创建项目。
     *
     * @param dto 包含项目名称和可选描述
     * @return 创建的项目视图
     */
    @PostMapping
    public BaseResponse<ProjectVO> create(@Valid @RequestBody CreateProjectDTO dto) {
        Long userId = BaseContext.getCurrentId();
        ProjectVO vo = projectService.createProject(dto.getName(), dto.getDescription(), userId);
        return BaseResponse.success(vo);
    }

    /**
     * 删除项目，级联将项目下文件移入回收站并通知 AI 模块清理向量。
     *
     * @param id 项目 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        projectService.deleteProject(id, userId);
        return BaseResponse.success();
    }

    /**
     * 编辑项目信息。
     *
     * @param id  项目 ID
     * @param dto 包含可选的新名称和新描述
     * @return 更新后的项目视图
     */
    @PutMapping("/{id}")
    public BaseResponse<ProjectVO> update(@PathVariable Long id, @Valid @RequestBody UpdateProjectDTO dto) {
        Long userId = BaseContext.getCurrentId();
        ProjectVO vo = projectService.updateProject(id, dto.getName(), dto.getDescription(), userId);
        return BaseResponse.success(vo);
    }

    /**
     * 查询项目详情。
     *
     * @param id 项目 ID
     * @return 项目视图
     */
    @GetMapping("/{id}")
    public BaseResponse<ProjectVO> detail(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        ProjectVO vo = projectService.getProject(id, userId);
        return BaseResponse.success(vo);
    }

    /**
     * 项目列表，按更新时间倒序分页。
     *
     * @param page 页码，默认 1
     * @param size 每页条数，默认 10
     * @return 分页项目列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<ProjectVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = BaseContext.getCurrentId();
        Page<ProjectVO> result = projectService.listProjects(page, size, userId);
        return BaseResponse.success(result);
    }

    /**
     * 归档项目。
     *
     * @param id 项目 ID
     * @return 操作成功无数据返回
     */
    @PutMapping("/{id}/archive")
    public BaseResponse<Void> archive(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        projectService.archiveProject(id, userId);
        return BaseResponse.success();
    }

    /**
     * 激活已归档项目。
     *
     * @param id 项目 ID
     * @return 操作成功无数据返回
     */
    @PutMapping("/{id}/activate")
    public BaseResponse<Void> activate(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        projectService.activateProject(id, userId);
        return BaseResponse.success();
    }
}
