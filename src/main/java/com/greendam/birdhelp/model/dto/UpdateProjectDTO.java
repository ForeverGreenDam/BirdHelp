package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * <p>
 * 编辑项目请求体，所有字段均为可选，只更新传入的字段。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class UpdateProjectDTO {

    /**
     * 项目名称，可选，最长 100 字符。
     */
    @Size(max = 100, message = "项目名称最长100个字符")
    private String name;

    /**
     * 项目描述，可选，最长 500 字符。
     */
    @Size(max = 500, message = "项目描述最长500个字符")
    private String description;
}
