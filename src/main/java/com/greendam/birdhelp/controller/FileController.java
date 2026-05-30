package com.greendam.birdhelp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.model.vo.FileRecordVO;
import com.greendam.birdhelp.model.vo.PreviewVO;
import com.greendam.birdhelp.service.FileService;
import com.greendam.birdhelp.service.FileStorageService;
import com.greendam.birdhelp.service.PreviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    @Resource
    private PreviewService previewService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 上传文件。
     *
     * @param file 上传的文件
     * @return 文件记录视图
     */
    @PostMapping("/upload")
    public BaseResponse<FileRecordVO> upload(@RequestParam("file") MultipartFile file,
                                             @RequestParam Long projectId) {
        Long userId = BaseContext.getCurrentId();
        FileRecordVO vo = fileService.upload(file, projectId, userId);
        return BaseResponse.success(vo);
    }

    /**
     * 下载文件（流式传输，不经过 byte[] 中转）。
     *
     * @param id 文件记录 ID
     */
    @GetMapping("/{id}/download")
    public void download(@PathVariable Long id, HttpServletResponse response) throws IOException {
        FileRecord record = fileService.getById(id);
        if (record == null || record.getDeleted() == 1) {
            writeError(response, 404, BaseResponse.error(ErrorCode.NOT_FOUND_ERROR));
            return;
        }

        String encodedName = URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);

        try {
            fileStorageService.download(record.getFileUrl(), response);
        } catch (FileNotFoundException e) {
            writeError(response, 404, BaseResponse.error(ErrorCode.NOT_FOUND_ERROR));
        }
    }

    private void writeError(HttpServletResponse response, int status, BaseResponse<?> body) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * 文件列表（含搜索），支持按类型/来源筛选、关键词模糊搜索、分页、按时间倒序。
     * 仅展示链尾文件（被 version_of 指向的旧版本自动隐藏）。
     *
     * @param page      页码，默认 1
     * @param size      每页条数，默认 10
     * @param fileType  文件类型（可选）：1-PPT 2-Word 3-PDF 4-图片 5-其他
     * @param source    文件来源（可选）：1-用户上传(知识库) 2-AI生成，不传则全部
     * @param keyword   搜索关键词（可选），按文件名模糊匹配
     * @param projectId 项目 ID
     * @return 分页文件列表
     */
    @GetMapping("/list")
    public BaseResponse<Page<FileRecordVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer fileType,
            @RequestParam(required = false) Integer source,
            @RequestParam(required = false) String keyword,
            @RequestParam Long projectId) {
        Long userId = BaseContext.getCurrentId();
        Page<FileRecordVO> result = fileService.listFiles(page, size, fileType, source, keyword, projectId, userId);
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
     * 获取文件预览数据（多级缓存：Redis → MySQL → 重新渲染）。
     * <p>渲染管道：LibreOffice 无头转 PDF → PDFBox 逐页渲染 150 DPI PNG → 上传 OSS。</p>
     *
     * @param id 文件记录 ID
     * @return 预览数据（fileId、totalPages、pages[{pageNumber, imageUrl, layoutType, title}]）
     */
    @GetMapping("/{id}/preview")
    public BaseResponse<PreviewVO> preview(@PathVariable Long id) {
        Long userId = BaseContext.getCurrentId();
        // 校验文件归属
        FileRecord record = fileService.getById(id);
        if (record == null || record.getDeleted() == 1) {
            return BaseResponse.error(ErrorCode.NOT_FOUND_ERROR, "文件不存在");
        }
        if (!record.getUserId().equals(userId)) {
            return BaseResponse.error(ErrorCode.NOT_AUTH_ERROR, "无权访问该文件");
        }
        PreviewVO vo = previewService.getPreview(id);
        return BaseResponse.success(vo);
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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Long projectId) {
        Long userId = BaseContext.getCurrentId();
        Page<FileRecordVO> result = fileService.recycleList(page, size, projectId, userId);
        return BaseResponse.success(result);
    }
}
