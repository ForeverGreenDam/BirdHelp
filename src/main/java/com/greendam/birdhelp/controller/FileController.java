package com.greendam.birdhelp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.vo.FileRecordVO;
import com.greendam.birdhelp.service.FileService;
import com.greendam.birdhelp.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 文件模块接口控制器，提供文件上传、下载、列表、搜索、回收站等功能。
 * </p>
 *
 * <h3>鉴权说明</h3>
 * <p>所有接口均需携带有效 JWT Token，仅允许操作本人的文件。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 上传文件。
     *
     * @param file 上传的文件
     * @return 文件记录视图
     */
    @PostMapping("/upload")
    public BaseResponse<FileRecordVO> upload(@RequestParam("file") MultipartFile file) {
        Long userId = BaseContext.getCurrentId();
        FileRecordVO vo = fileService.upload(file, userId);
        return BaseResponse.success(vo);
    }

    /**
     * 下载文件。
     *
     * @param id 文件记录 ID
     * @return 文件字节内容
     */
    @GetMapping("/{id}/download")
    public byte[] download(@PathVariable Long id, javax.servlet.http.HttpServletResponse response) {
        FileRecordVO vo = fileService.getFileRecord(id);
        byte[] content = fileStorageService.load(
                fileService.getById(id).getFileUrl());
        if (content == null) {
            response.setStatus(404);
            return new byte[0];
        }
        String encodedName = URLEncoder.encode(vo.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encodedName);
        return content;
    }

    /**
     * 文件列表，支持按类型筛选、分页、按时间倒序。
     *
     * @param page     页码，默认 1
     * @param size     每页条数，默认 10
     * @param fileType 文件类型（可选）：1-PPT 2-Word 3-PDF 4-图片 5-其他
     * @return 分页文件列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<FileRecordVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer fileType) {
        Long userId = BaseContext.getCurrentId();
        Page<FileRecordVO> result = fileService.listFiles(page, size, fileType, userId);
        return BaseResponse.success(result);
    }

    /**
     * 按文件名模糊搜索。
     *
     * @param keyword 搜索关键词
     * @param page    页码，默认 1
     * @param size    每页条数，默认 10
     * @return 分页搜索结果
     */
    @GetMapping("/search")
    public BaseResponse<Page<FileRecordVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = BaseContext.getCurrentId();
        Page<FileRecordVO> result = fileService.searchFiles(keyword, page, size, userId);
        return BaseResponse.success(result);
    }

    /**
     * 删除文件（移入回收站）。
     *
     * @param id 文件记录 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Void> softDelete(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        fileService.softDelete(id, userId);
        return BaseResponse.success();
    }

    /**
     * 从回收站恢复文件。
     *
     * @param id 文件记录 ID
     * @return 操作成功无数据返回
     */
    @PutMapping("/{id}/restore")
    public BaseResponse<Void> restore(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        fileService.restore(id, userId);
        return BaseResponse.success();
    }

    /**
     * 永久删除文件（删除物理文件及数据库记录）。
     *
     * @param id 文件记录 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/{id}/permanent")
    public BaseResponse<Void> permanentDelete(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        fileService.permanentDelete(id, userId);
        return BaseResponse.success();
    }

    /**
     * 回收站列表。
     *
     * @param page 页码，默认 1
     * @param size 每页条数，默认 10
     * @return 分页回收站文件列表
     */
    @GetMapping("/recycle")
    public BaseResponse<Page<FileRecordVO>> recycleList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = BaseContext.getCurrentId();
        Page<FileRecordVO> result = fileService.recycleList(page, size, userId);
        return BaseResponse.success(result);
    }
}
