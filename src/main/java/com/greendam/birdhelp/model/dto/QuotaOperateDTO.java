package com.greendam.birdhelp.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 额度操作请求体，用于 AI 模块内部调用扣减/退还额度。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
public class QuotaOperateDTO {

    /**
     * 用户 ID，不可为空。
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 关联业务 ID（如生成任务 ID），用于流水记录。
     */
    private Long relatedId;
}
