package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.common.utils.DocGenerationPublisher;
import com.greendam.birdhelp.model.dto.GeneratePdfDTO;
import com.greendam.birdhelp.model.vo.DocGenerateTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.UUID;

/**
 * PDF 生成接口，提交异步任务至 RabbitMQ。
 */
@Slf4j
@RestController
@RequestMapping("/pdf")
public class PdfController {

    @Resource
    private DocGenerationPublisher publisher;

    /**
     * 提交 PDF 生成任务（异步，立即返回 taskId）。
     */
    @PostMapping("/generate")
    public BaseResponse<DocGenerateTaskVO> generate(@Valid @RequestBody GeneratePdfDTO dto) {
        Long userId = BaseContext.getCurrentId();
        String callbackId = UUID.randomUUID().toString();
        log.info("收到 PDF 生成请求: userId={}, projectId={}, topic={}, callbackId={}",
                userId, dto.getProjectId(), dto.getTopic(), callbackId);

        DocGenerateTaskVO task = publisher.publishPdf(
                String.valueOf(userId),
                dto.getProjectId(),
                dto.getTopic(),
                dto.getLanguage(),
                dto.getDocType(),
                dto.getStyle(),
                dto.getExtraPrompt(),
                dto.getEnableImages(),
                dto.getMaterialIds(),
                dto.getRagEnabled(),
                callbackId
        );
        return BaseResponse.success(task);
    }
}
