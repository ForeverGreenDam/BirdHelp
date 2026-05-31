package com.greendam.birdhelp.model.dto;

import lombok.Data;

/**
 * <p>
 * 创建新会话请求体，前端选中文件后调用，Java 端生成 sessionId 并入库。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code fileId}：选中的源文件 ID</li>
 *   <li>{@code docType}：文档类型（ppt / word / pdf）</li>
 *   <li>{@code projectId}：所属项目 ID</li>
 *   <li>{@code title}：会话标题（可选，不传则取文件名去扩展名）</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
public class CreateSessionDTO {

    /**
     * 源文件 ID
     */
    private String fileId;

    /**
     * 文档类型：ppt / word / pdf
     */
    private String docType;

    /**
     * 所属项目 ID
     */
    private Long projectId;

    /**
     * 会话标题（可选），不传则取原始文件名去扩展名
     */
    private String title;
}
