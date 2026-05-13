package com.greendam.birdhelp.internal;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.model.dto.FileInternalUploadDTO;
import com.greendam.birdhelp.model.vo.FileRecordVO;
import com.greendam.birdhelp.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;

/**
 * <p>
 * 内部文件接口控制器，供 AI 模块调用上传生成结果文件，不对外暴露。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/internal")
public class FileInternalController {

    @Resource
    private FileService fileService;

    /**
     * AI 模块上传生成结果文件。
     *
     * @param file 上传的文件
     * @param dto  包含用户 ID 和文件名的请求参数
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
            return BaseResponse.error(
                    com.greendam.birdhelp.exception.ErrorCode.OPERATION_ERROR,
                    "文件上传失败");
        }
    }
}
