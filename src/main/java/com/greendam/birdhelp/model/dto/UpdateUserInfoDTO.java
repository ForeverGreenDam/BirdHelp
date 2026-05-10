package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * <p>
 * 修改个人信息请求体。
 * </p>
 *
 * <p>所有字段均为可选，仅需传入想要修改的字段，未传入的字段保持原值不变。</p>
 *
 * @author ForeverGreenDam
 */
@Data
public class UpdateUserInfoDTO {

    /**
     * 用户昵称，最长 50 位。
     */
    @Size(max = 50, message = "昵称最长50位")
    private String nickname;

    /**
     * 头像文件的 OSS URL 地址。
     */
    private String avatar;

    /**
     * 性别：{@code 0} - 未知，{@code 1} - 男，{@code 2} - 女。
     */
    private Integer sex;

    /**
     * 出生日期，格式 {@code yyyy-MM-dd}。
     */
    private String birthday;
}
