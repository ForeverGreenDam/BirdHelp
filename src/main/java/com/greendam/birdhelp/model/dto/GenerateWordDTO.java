package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * <p>
 * Word 生成请求体。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class GenerateWordDTO {

    /**
     * 项目 ID，用于知识库隔离
     */
    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    /**
     * 文档主题，如 "人工智能发展报告"
     */
    @NotBlank(message = "文档主题不能为空")
    @Size(max = 200, message = "主题最长200个字符")
    private String topic;

    /**
     * 语言：zh（中文）/ en（英文），默认 zh
     */
    private String language;

    /**
     * 文档类型：essay（论文）/ report（报告）/ letter（信函）/ paper（学术论文），默认 essay
     */
    private String docType;

    /**
     * 目标字数，范围 500–10000，默认 2000
     */
    private Integer wordCount;

    /**
     * 用户补充指令，如 "重点阐述深度学习部分"
     */
    @Size(max = 500, message = "补充指令最长500个字符")
    private String extraPrompt;

    /**
     * RAG 参考素材的 javaFileId 列表
     */
    private List<String> materialIds;

    /**
     * 是否启用 RAG 检索增强，默认 false
     */
    private Boolean ragEnabled;
}
