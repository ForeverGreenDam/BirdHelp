package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 内部文件上传请求体，供 AI 模块调用上传生成结果文件。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class FileInternalUploadDTO {

    /**
     * 用户 ID，不可为空。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 项目 ID，不可为空。
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 原始文件名（含扩展名），不可为空。
     */
    @NotNull(message = "文件名不能为空")
    private String fileName;
}
