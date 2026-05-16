package com.greendam.birdhelp.model.vo;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * PPT 生成结果视图对象。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class PptGenerateResultVO {

    /**
     * 生成的文件 ID（Java 端存储）
     */
    private Long fileId;

    /**
     * 文件访问 URL
     */
    private String fileUrl;

    /**
     * 文件名
     */
    private String fileName;
}
