package com.greendam.birdhelp.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>
 * 会员状态响应对象，用于查询当前用户的会员状态。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatusVO {

    /**
     * 当前会员等级：{@code 0} - 免费，{@code 1} - 月卡，{@code 2} - 季卡，{@code 3} - 年卡。
     */
    private Integer memberLevel;

    /**
     * 会员等级名称，如 "免费用户"、"月卡"、"季卡"、"年卡"。
     */
    private String memberLevelName;

    /**
     * 会员到期时间，免费用户为 {@code null}。
     */
    private LocalDateTime memberExpireAt;

    /**
     * 是否已过期。
     */
    private Boolean isExpired;

    /**
     * 每日额度上限。
     */
    private Integer dailyLimit;
}
