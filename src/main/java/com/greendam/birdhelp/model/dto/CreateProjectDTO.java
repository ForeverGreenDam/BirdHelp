package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 创建项目请求体。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class CreateProjectDTO {

    /**
     * 项目名称，不可为空，最长 100 字符。
     */
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称最长100个字符")
    private String name;

    /**
     * 项目描述，可选，最长 500 字符。
     */
    @Size(max = 500, message = "项目描述最长500个字符")
    private String description;
}
