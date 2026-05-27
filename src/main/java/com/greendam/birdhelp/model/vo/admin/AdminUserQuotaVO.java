package com.greendam.birdhelp.model.vo.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 管理员用户额度视图对象，用于展示指定用户的额度使用概况。
 * </p>
 *
 * <h3>字段说明</h3>
 * <ul>
 *   <li>{@code userId} - 用户 ID</li>
 *   <li>{@code username} - 用户名</li>
 *   <li>{@code nickname} - 用户昵称</li>
 *   <li>{@code memberLevel} - 会员等级：{@code 0} - 普通用户，{@code 1} - 会员</li>
 *   <li>{@code memberExpireAt} - 会员过期时间</li>
 *   <li>{@code dailyUsed} - 当日已用额度</li>
 *   <li>{@code dailyLimit} - 每日额度上限</li>
 *   <li>{@code dailyDate} - 额度所属日期</li>
 * </ul>
 *
 * @author ForeverGreenDam
 */
@Data
@Builder
public class AdminUserQuotaVO {
    /**
     * 用户 ID。
     */
    private Long userId;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 用户昵称。
     */
    private String nickname;
    /**
     * 会员等级：{@code 0} - 普通用户，{@code 1} - 会员。
     */
    private Integer memberLevel;
    /**
     * 会员过期时间。
     */
    private LocalDateTime memberExpireAt;
    /**
     * 当日已用额度。
     */
    private Integer dailyUsed;
    /**
     * 每日额度上限。
     */
    private Integer dailyLimit;
    /**
     * 额度所属日期。
     */
    private LocalDate dailyDate;
}
