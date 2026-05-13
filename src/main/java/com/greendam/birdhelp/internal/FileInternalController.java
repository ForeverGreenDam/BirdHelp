package com.greendam.birdhelp.internal;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.model.dto.FileInternalUploadDTO;
import com.greendam.birdhelp.model.entity.FileRecord;
import com.greendam.birdhelp.model.vo.FileRecordVO;
import com.greendam.birdhelp.service.FileService;
import com.greendam.birdhelp.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 内部文件接口控制器，供 AI 模块调用，不对外暴露。
 * </p>
 *
 * <h3>鉴权说明</h3>
 * <p>所有接口均通过 {@code SignFilter} 进行 RSA 签名校验，无需 JWT Token。</p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/internal")
public class FileInternalController {

    @Resource
    private FileService fileService;

    @Resource
    private FileStorageService fileStorageService;

    /**
     * AI 模块上传文件（素材文件 / 生成结果文件）。
     *
     * @param file 上传的文件
     * @param dto  包含 userId、projectId、fileName 的请求参数
     * @return 文件记录视图
     */
    @PostMapping("/file/upload")
    public BaseResponse<FileRecordVO> upload(
            @RequestParam("file") MultipartFile file,
            @Valid FileInternalUploadDTO dto) {
        try {
            FileRecordVO vo = fileService.uploadByAi(file.getBytes(),
                    dto.getFileName(), dto.getProjectId(), dto.getUserId());
            return BaseResponse.success(vo);
        } catch (IOException e) {
            log.error("AI 模块文件上传失败", e);
            return BaseResponse.error(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }
    }

    /**
     * AI 模块下载文件，用于向量化等处理。
     *
     * @param id 文件记录 ID
     * @return 文件二进制内容
     */
    @GetMapping("/file/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        FileRecord record = fileService.getById(id);
        if (record == null || record.getDeleted() == 1) {
            return ResponseEntity.notFound().build();
        }

        byte[] content = fileStorageService.load(record.getFileUrl());
        if (content == null) {
            return ResponseEntity.notFound().build();
        }

        String encodedName = URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"")
                .body(content);
    }

    /**
     * AI 模块软删除文件（移入回收站）。
     *
     * @param id     文件记录 ID
     * @param userId 用户 ID
     * @return 操作成功无数据返回
     */
    @DeleteMapping("/file/{id}")
    public BaseResponse<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        fileService.softDelete(id, userId);
        log.info("AI 模块软删除文件: id={}, userId={}", id, userId);
        return BaseResponse.success();
    }
}
