package com.greendam.birdhelp.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户额度信息视图对象，用于 {@code GET /api/quota/my} 接口返回。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class QuotaInfoVO {

    /** 今日剩余可用次数 */
    private Integer todayRemaining;

    /** 每日额度上限 */
    private Integer dailyLimit;

    /** 今日已用次数 */
    private Integer dailyUsed;

    /** 当前会员等级：0-免费，1-月卡，2-季卡，3-年卡 */
    private Integer memberLevel;

    /** 会员到期时间，免费用户为 {@code null} */
    private LocalDateTime memberExpireAt;
}
