package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.entity.Project;
import com.greendam.birdhelp.service.ProjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 管理员项目管理接口控制器，提供项目的分页查询、详情查看及删除功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>项目列表：分页查询所有项目，支持按名称、用户 ID 和状态筛选</li>
 *   <li>项目详情：查看指定项目的完整信息</li>
 *   <li>项目删除：删除指定项目及其关联资源</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/project")
public class AdminProjectController {

    @Resource
    private ProjectService projectService;

    /**
     * <p>分页查询项目列表。</p>
     *
     * <p>支持按项目名称、所属用户 ID 和项目状态进行筛选。</p>
     *
     * @param page   页码，默认为 1
     * @param size   每页条数，默认为 10
     * @param name   项目名称（可选），模糊匹配
     * @param userId 用户 ID（可选），按创建者筛选
     * @param status 项目状态（可选），如启用、禁用等
     * @return 项目分页数据
     */
    @GetMapping("/list")
    public BaseResponse<Page<Project>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        return BaseResponse.success(projectService.adminListProjects(page, size, name, userId, status));
    }

    /**
     * <p>查询指定项目的详细信息。</p>
     *
     * @param id 项目 ID
     * @return 项目实体对象
     */
    @GetMapping("/{id}")
    public BaseResponse<Project> detail(@PathVariable Long id) {
        return BaseResponse.success(projectService.getById(id));
    }

    /**
     * <p>删除指定项目。</p>
     *
     * <p>同时会清理项目关联的文件记录和文档生成任务等资源。</p>
     *
     * @param id 待删除的项目 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        projectService.adminDeleteProject(id);
        return BaseResponse.success();
    }
}
