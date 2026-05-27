package com.greendam.birdhelp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.model.dto.admin.QuotaConfigUpdateDTO;
import com.greendam.birdhelp.model.dto.admin.UserQuotaMemberUpdateDTO;
import com.greendam.birdhelp.model.entity.QuotaConfig;
import com.greendam.birdhelp.model.entity.UserQuota;
import com.greendam.birdhelp.model.vo.QuotaInfoVO;
import com.greendam.birdhelp.model.vo.admin.AdminQuotaLogVO;
import com.greendam.birdhelp.model.vo.admin.AdminUserQuotaVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 额度服务接口，继承 MyBatis-Plus {@link IService} 获得通用 CRUD 能力。
 * </p>
 *
 * <h3>额度扣减策略</h3>
 * <p>先查会员额度（会员有效期内），额度用尽或会员过期则回退到免费额度。</p>
 * <p>每次操作写入 {@code quota_log} 流水表，便于对账追溯。</p>
 *
 * @author ForeverGreenDam
 * @see com.greendam.birdhelp.service.impl.QuotaServiceImpl
 */
public interface QuotaService extends IService<UserQuota> {

    /**
     * <p>查询当前登录用户的额度信息。</p>
     *
     * <p>实时计算今日剩余：{@code 当日额度上限 - 今日已用次数}。
     * 若 {@code daily_date} 不是今天，自动重置 {@code daily_used} 为 0。</p>
     *
     * @param userId 用户 ID
     * @return 额度信息视图，包含今日剩余、每日上限、已用次数、会员等级及到期时间
     */
    QuotaInfoVO getMyQuota(Long userId);

    /**
     * <p>扣减一次额度。</p>
     *
     * <p>由 AI 模块在生成任务提交时调用。先判断当前有效等级（会员未过期用会员额度，否则用免费额度），
     * 校验额度未超限后扣减并记录流水。</p>
     *
     * @param userId    用户 ID
     * @param relatedId 关联业务 ID（生成任务 callback_id），可为空
     * @throws com.greendam.birdhelp.exception.BusinessException 错误码：
     *         <ul>
     *           <li>{@code QUOTA_EXCEEDED(40009)} — 当日额度已用完</li>
     *         </ul>
     */
    void consumeQuota(Long userId, String relatedId);

    /**
     * <p>退还一次额度。</p>
     *
     * <p>由 AI 模块在生成失败时调用。将 {@code daily_used} 回退 1 次（最低为 0），并记录流水。</p>
     *
     * @param userId    用户 ID
     * @param relatedId 关联业务 ID（生成任务 callback_id），可为空
     */
    void refundQuota(Long userId, String relatedId);

    List<QuotaConfig> adminListConfigs();

    void adminUpdateConfig(QuotaConfigUpdateDTO dto);

    Page<AdminUserQuotaVO> adminListUserQuotas(int page, int size, Long userId, Integer memberLevel);

    void adminChangeMemberLevel(UserQuotaMemberUpdateDTO dto);

    Page<AdminQuotaLogVO> adminListQuotaLogs(int page, int size, Long userId, Integer changeType, LocalDateTime startTime, LocalDateTime endTime);
}
