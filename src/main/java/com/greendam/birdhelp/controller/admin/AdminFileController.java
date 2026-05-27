package com.greendam.birdhelp.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 管理员文件管理接口控制器，提供文件记录的分页查询和删除功能。
 * </p>
 *
 * <h3>功能说明</h3>
 * <ul>
 *   <li>文件列表：分页查询所有文件记录，支持按用户、项目、文件名和文件类型筛选</li>
 *   <li>文件删除：删除指定文件记录及底层存储文件</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/admin/file")
public class AdminFileController {

    @Resource
    private FileService fileService;

    /**
     * <p>分页查询文件记录列表。</p>
     *
     * <p>支持按用户 ID、项目 ID、文件名称和文件类型进行筛选。</p>
     *
     * @param page      页码，默认为 1
     * @param size      每页条数，默认为 10
     * @param userId    用户 ID（可选）
     * @param projectId 项目 ID（可选）
     * @param fileName  文件名称（可选），模糊匹配
     * @param fileType  文件类型（可选），如 Word、PDF、PPT 等
     * @return 文件记录分页数据
     */
    @GetMapping("/list")
    public BaseResponse<Page<FileRecord>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) Integer fileType) {
        return BaseResponse.success(fileService.adminListFiles(page, size, userId, projectId, fileName, fileType));
    }

    /**
     * <p>删除指定文件记录。</p>
     *
     * <p>同时删除底层存储在 OSS 中的实际文件。</p>
     *
     * @param id 待删除的文件记录 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id) {
        fileService.adminDeleteFile(id);
        return BaseResponse.success();
    }
}
