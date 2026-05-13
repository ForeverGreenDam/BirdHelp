package com.greendam.birdhelp.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 项目视图对象，用于项目列表和详情接口返回。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class ProjectVO {

    /** 项目 ID */
    private Long id;

    /** 项目名称 */
    private String name;

    /** 项目描述 */
    private String description;

    /** 状态：0-已归档，1-活跃 */
    private Integer status;

    /** 文件数量 */
    private Integer fileCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
