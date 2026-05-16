package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.common.utils.AiModuleCaller;
import com.greendam.birdhelp.model.dto.GeneratePptDTO;
import com.greendam.birdhelp.model.vo.PptGenerateResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.UUID;

/**
 * <p>
 * PPT 生成模块接口控制器，代理转发至 AI 模块。
 * </p>
 *
 * <h3>调用流程</h3>
 * <ol>
 *   <li>前端发起 POST /ppt/generate</li>
 *   <li>Java 后端签名后转发至 AI 模块 POST /ai/ppt/generate</li>
 *   <li>AI 模块同步生成 PPT（阻塞 20–60 秒）并上传至 Java 存储</li>
 *   <li>Java 后端将生成结果返回前端</li>
 * </ol>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/ppt")
public class PptController {

    @Resource
    private AiModuleCaller aiModuleCaller;

    /**
     * 生成 PPT 文档。
     *
     * <p>同步接口，请求会阻塞 20–60 秒直到 AI 模块生成完成。</p>
     *
     * @param dto PPT 生成请求体
     * @return 生成结果（文件 ID、URL、文件名）
     */
    @PostMapping("/generate")
    public BaseResponse<PptGenerateResultVO> generate(@Valid @RequestBody GeneratePptDTO dto) {
        Long userId = BaseContext.getCurrentId();
        String callbackId = UUID.randomUUID().toString();
        log.info("收到 PPT 生成请求: userId={}, projectId={}, topic={}, callbackId={}",
                userId, dto.getProjectId(), dto.getTopic(), callbackId);

        PptGenerateResultVO result = aiModuleCaller.generatePpt(
                String.valueOf(userId),
                dto.getProjectId(),
                dto.getTopic(),
                dto.getLanguage(),
                dto.getStyle(),
                dto.getSlideCount(),
                dto.getExtraPrompt(),
                dto.getMaterialIds(),
                dto.getRagEnabled(),
                callbackId
        );
        return BaseResponse.success(result);
    }
}
