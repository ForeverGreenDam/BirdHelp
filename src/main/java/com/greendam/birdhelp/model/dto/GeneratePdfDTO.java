package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * <p>
 * PDF 生成请求体。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class GeneratePdfDTO {

    /**
     * 项目 ID，用于知识库隔离
     */
    @NotBlank(message = "项目ID不能为空")
    private String projectId;

    /**
     * 文档主题，如 "年度工作总结"
     */
    @NotBlank(message = "文档主题不能为空")
    @Size(max = 200, message = "主题最长200个字符")
    private String topic;

    /**
     * 语言：zh（中文）/ en（英文），默认 zh
     */
    private String language;

    /**
     * 文档类型：report（报告）/ resume（简历）/ form（表单），默认 report
     */
    private String docType;

    /**
     * 风格：academic（学术）/ business（商务）/ creative（创意）/ minimal（极简）/ tech（科技）/ warm（暖色），默认 academic
     */
    private String style;

    /**
     * 用户补充指令，如 "突出Q3业绩数据"
     */
    @Size(max = 500, message = "补充指令最长500个字符")
    private String extraPrompt;

    /**
     * 是否自动搜索配图（Unsplash → Pexels → 纯色占位图降级），默认 true
     */
    private Boolean enableImages;

    /**
     * RAG 参考素材的 javaFileId 列表
     */
    private List<String> materialIds;

    /**
     * 是否启用 RAG 检索增强，默认 false
     */
    private Boolean ragEnabled;
}
