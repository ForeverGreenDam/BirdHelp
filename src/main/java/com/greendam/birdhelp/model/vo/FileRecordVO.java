package com.greendam.birdhelp.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 文件记录视图对象，用于文件列表和搜索接口返回。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class FileRecordVO {

    /** 文件记录 ID */
    private Long id;

    /** 原始文件名 */
    private String fileName;

    /** 文件类型：1-PPT，2-Word，3-PDF，4-图片，5-其他 */
    private Integer fileType;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 文件来源：1-用户上传，2-AI生成 */
    private Integer source;

    /** 回收站标记：0-正常，1-回收站 */
    private Integer deleted;

    /** 移入回收站时间 */
    private LocalDateTime deletedAt;

    /** 创建时间 */
    private LocalDateTime createTime;
}
