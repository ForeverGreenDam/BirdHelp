package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.QuotaConfigMapper;
import com.greendam.birdhelp.mapper.QuotaLogMapper;
import com.greendam.birdhelp.mapper.SysUserMapper;
import com.greendam.birdhelp.mapper.UserQuotaMapper;
import com.greendam.birdhelp.model.dto.admin.QuotaConfigUpdateDTO;
import com.greendam.birdhelp.model.dto.admin.UserQuotaAdjustDTO;
import com.greendam.birdhelp.model.dto.admin.UserQuotaMemberUpdateDTO;
import com.greendam.birdhelp.model.entity.QuotaConfig;
import com.greendam.birdhelp.model.entity.QuotaLog;
import com.greendam.birdhelp.model.entity.SysUser;
import com.greendam.birdhelp.model.entity.UserQuota;
import com.greendam.birdhelp.model.vo.QuotaInfoVO;
import com.greendam.birdhelp.model.vo.admin.AdminQuotaLogVO;
import com.greendam.birdhelp.model.vo.admin.AdminUserQuotaVO;
import com.greendam.birdhelp.service.QuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 额度服务实现类，继承 MyBatis-Plus {@link ServiceImpl} 提供通用 CRUD 实现。
 * </p>
 *
 * <h3>额度扣减流程</h3>
 * <ol>
 *   <li>获取或创建用户额度记录，跨天自动重置</li>
 *   <li>判断有效等级：会员未过期用会员额度，已过期用免费额度</li>
 *   <li>查 {@code quota_config} 获取对应等级的每日上限</li>
 *   <li>校验当日已用次数未超限</li>
 *   <li>扣减并记录流水</li>
 * </ol>
 *
 * <h3>并发控制</h3>
 * <p>扣减和退还操作使用 Redis 分布式锁，按 {@code user_id} 粒度串行化，
 * 防止同一用户并发请求导致超限扣减。</p>
 *
 * @author ForeverGreenDam
 * @see QuotaService
 */
@Slf4j
@Service
public class QuotaServiceImpl extends ServiceImpl<UserQuotaMapper, UserQuota>
        implements QuotaService {

    @Resource
    private QuotaConfigMapper quotaConfigMapper;

    @Resource
    private QuotaLogMapper quotaLogMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 免费等级 */
    private static final int LEVEL_FREE = 0;
    /** 默认已用次数 */
    private static final int DEFAULT_USED = 0;
    /** Redis 锁键前缀 */
    private static final String LOCK_PREFIX = "quota_lock:";
    /** 锁超时时间（秒） */
    private static final long LOCK_TIMEOUT = 5;

    // ==================== 公开方法 ====================

    /**
     * <p>查询当前用户的额度信息，实时计算今日剩余次数。</p>
     *
     * <p>首次查询的用户自动创建 {@code user_quota} 记录（免费等级、已用 0 次）。
     * 每日首次查询时自动重置已用次数。</p>
     *
     * @param userId 用户 ID
     * @return 额度信息视图
     */
    @Override
    public QuotaInfoVO getMyQuota(Long userId) {
        UserQuota quota = getOrCreateUserQuota(userId);
        resetIfCrossDay(quota);

        int effectiveLevel = getEffectiveLevel(quota);
        int dailyLimit = getDailyLimit(effectiveLevel);

        return QuotaInfoVO.builder()
                .todayRemaining(Math.max(0, dailyLimit - quota.getDailyUsed()))
                .dailyLimit(dailyLimit)
                .dailyUsed(quota.getDailyUsed())
                .memberLevel(quota.getMemberLevel())
                .memberExpireAt(quota.getMemberExpireAt())
                .build();
    }

    /**
     * <p>扣减一次额度，由 AI 模块在生成任务提交前调用。</p>
     *
     * <p>使用 Redis 分布式锁保证同一用户串行操作，防止并发超限。</p>
     *
     * @param userId    用户 ID
     * @param relatedId 关联业务 ID
     * @throws BusinessException {@code QUOTA_EXCEEDED} — 当日额度已用完
     */
    @Override
    public void consumeQuota(Long userId, String relatedId) {
        String lockKey = LOCK_PREFIX + userId;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(LOCK_TIMEOUT));
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        try {
            doConsume(userId, relatedId);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * <p>退还一次额度，由 AI 模块在生成失败时调用。</p>
     *
     * <p>使用 Redis 分布式锁保证同一用户串行操作。已用次数最低退至 0。</p>
     *
     * @param userId    用户 ID
     * @param relatedId 关联业务 ID
     */
    @Override
    public void refundQuota(Long userId, String relatedId) {
        String lockKey = LOCK_PREFIX + userId;
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(LOCK_TIMEOUT));
        if (Boolean.FALSE.equals(locked)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        try {
            doRefund(userId, relatedId);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    // ==================== 管理员方法 ====================

    @Override
    public List<QuotaConfig> adminListConfigs() {
        return quotaConfigMapper.selectList(null);
    }

    @Override
    public void adminUpdateConfig(QuotaConfigUpdateDTO dto) {
        QuotaConfig config = quotaConfigMapper.selectById(dto.getId());
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "额度配置不存在");
        }
        config.setDailyLimit(dto.getDailyLimit());
        quotaConfigMapper.updateById(config);
        log.info("管理员更新额度配置: level={}, dailyLimit={}", config.getLevel(), dto.getDailyLimit());
    }

    @Override
    public Page<AdminUserQuotaVO> adminListUserQuotas(int page, int size, Long userId, Integer memberLevel) {
        LambdaQueryWrapper<UserQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, UserQuota::getUserId, userId)
                .eq(memberLevel != null, UserQuota::getMemberLevel, memberLevel)
                .orderByAsc(UserQuota::getUserId);

        Page<UserQuota> quotaPage = page(Page.of(page, size), wrapper);

        Map<Long, SysUser> userMap = loadUserMap(quotaPage.getRecords().stream()
                .map(UserQuota::getUserId).collect(Collectors.toList()));

        Page<AdminUserQuotaVO> voPage = new Page<>(page, size, quotaPage.getTotal());
        voPage.setRecords(quotaPage.getRecords().stream().map(q -> {
            SysUser user = userMap.get(q.getUserId());
            int effectiveLevel = getEffectiveLevel(q);
            int dailyLimit = getDailyLimit(effectiveLevel);
            return AdminUserQuotaVO.builder()
                    .userId(q.getUserId())
                    .username(user != null ? user.getUsername() : "")
                    .nickname(user != null ? user.getNickname() : "")
                    .memberLevel(q.getMemberLevel())
                    .memberExpireAt(q.getMemberExpireAt())
                    .dailyUsed(q.getDailyUsed())
                    .dailyLimit(dailyLimit)
                    .dailyDate(q.getDailyDate())
                    .build();
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void adminAdjustQuota(UserQuotaAdjustDTO dto) {
        UserQuota quota = getOrCreateUserQuota(dto.getUserId());
        int newVal = quota.getDailyUsed() + dto.getChangeAmount();
        if (newVal < 0) newVal = 0;
        quota.setDailyUsed(newVal);
        quota.setDailyDate(LocalDate.now());
        updateById(quota);
        insertLog(dto.getUserId(), dto.getChangeAmount() > 0 ? 1 : 2, "admin_manual");
        log.info("管理员手动调整额度: userId={}, change={}, newDailyUsed={}", dto.getUserId(), dto.getChangeAmount(), newVal);
    }

    @Override
    public void adminChangeMemberLevel(UserQuotaMemberUpdateDTO dto) {
        UserQuota quota = getOrCreateUserQuota(dto.getUserId());
        quota.setMemberLevel(dto.getMemberLevel());
        quota.setMemberExpireAt(dto.getMemberExpireAt());
        updateById(quota);
        log.info("管理员修改会员等级: userId={}, level={}, expireAt={}", dto.getUserId(), dto.getMemberLevel(), dto.getMemberExpireAt());
    }

    @Override
    public Page<AdminQuotaLogVO> adminListQuotaLogs(int page, int size, Long userId, Integer changeType,
                                                    LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<QuotaLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, QuotaLog::getUserId, userId)
                .eq(changeType != null, QuotaLog::getChangeType, changeType)
                .ge(startTime != null, QuotaLog::getCreateTime, startTime)
                .le(endTime != null, QuotaLog::getCreateTime, endTime)
                .orderByDesc(QuotaLog::getCreateTime);

        Page<QuotaLog> logPage = quotaLogMapper.selectPage(Page.of(page, size), wrapper);

        Map<Long, SysUser> userMap = loadUserMap(logPage.getRecords().stream()
                .map(QuotaLog::getUserId).collect(Collectors.toList()));

        Page<AdminQuotaLogVO> voPage = new Page<>(page, size, logPage.getTotal());
        voPage.setRecords(logPage.getRecords().stream().map(l -> {
            SysUser user = userMap.get(l.getUserId());
            return AdminQuotaLogVO.builder()
                    .id(l.getId())
                    .userId(l.getUserId())
                    .username(user != null ? user.getUsername() : "")
                    .changeType(l.getChangeType())
                    .relatedId(l.getRelatedId())
                    .createTime(l.getCreateTime())
                    .build();
        }).collect(Collectors.toList()));
        return voPage;
    }

    /**
     * 批量加载用户信息映射。
     */
    private Map<Long, SysUser> loadUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()));
        return users.stream().collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    // ==================== 内部方法 ====================

    /**
     * 执行额度扣减（无锁版本，由持有锁的调用方使用）。
     */
    private void doConsume(Long userId, String relatedId) {
        UserQuota quota = getOrCreateUserQuota(userId);
        resetIfCrossDay(quota);

        int effectiveLevel = getEffectiveLevel(quota);
        int dailyLimit = getDailyLimit(effectiveLevel);

        if (quota.getDailyUsed() >= dailyLimit) {
            throw new BusinessException(ErrorCode.QUOTA_EXCEEDED);
        }

        quota.setDailyUsed(quota.getDailyUsed() + 1);
        updateById(quota);

        insertLog(userId, 1, relatedId);
        log.info("额度扣减: userId={}, dailyUsed={}/{}, relatedId={}",
                userId, quota.getDailyUsed(), dailyLimit, relatedId);
    }

    /**
     * 执行额度退还（无锁版本，由持有锁的调用方使用）。
     */
    private void doRefund(Long userId, String relatedId) {
        UserQuota quota = getOrCreateUserQuota(userId);

        if (quota.getDailyUsed() > 0) {
            quota.setDailyUsed(quota.getDailyUsed() - 1);
            updateById(quota);
        }

        insertLog(userId, 2, relatedId);
        log.info("额度退还: userId={}, dailyUsed={}, relatedId={}",
                userId, quota.getDailyUsed(), relatedId);
    }

    /**
     * 获取或创建用户额度记录。不存在时自动创建免费等级记录。
     *
     * @param userId 用户 ID
     * @return 用户额度实体
     */
    private UserQuota getOrCreateUserQuota(Long userId) {
        UserQuota quota = lambdaQuery().eq(UserQuota::getUserId, userId).one();
        if (quota == null) {
            quota = new UserQuota();
            quota.setUserId(userId);
            quota.setMemberLevel(LEVEL_FREE);
            quota.setDailyUsed(DEFAULT_USED);
            quota.setDailyDate(LocalDate.now());
            save(quota);
        }
        return quota;
    }

    /**
     * 跨天重置：若 {@code daily_date} 不是今天，清零已用次数并更新日期。
     *
     * @param quota 用户额度记录
     */
    private void resetIfCrossDay(UserQuota quota) {
        LocalDate today = LocalDate.now();
        if (!today.equals(quota.getDailyDate())) {
            quota.setDailyUsed(DEFAULT_USED);
            quota.setDailyDate(today);
            updateById(quota);
        }
    }

    /**
     * 判断当前有效等级：会员未过期返回会员等级，已过期或无会员返回免费等级。
     *
     * @param quota 用户额度记录
     * @return 有效等级
     */
    private int getEffectiveLevel(UserQuota quota) {
        LocalDateTime expireAt = quota.getMemberExpireAt();
        if (expireAt != null && expireAt.isAfter(LocalDateTime.now())) {
            return quota.getMemberLevel();
        }
        return LEVEL_FREE;
    }

    /**
     * 查询指定等级的每日额度上限。
     *
     * @param level 会员等级
     * @return 每日生成次数上限
     * @throws BusinessException {@code NOT_FOUND_ERROR} — 配置表中无该等级记录
     */
    private int getDailyLimit(int level) {
        QuotaConfig config = quotaConfigMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuotaConfig>()
                        .eq(QuotaConfig::getLevel, level)
        );
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return config.getDailyLimit();
    }

    /**
     * 插入额度变更流水记录。
     *
     * @param userId     用户 ID
     * @param changeType 变更类型：1-扣减，2-退还
     * @param relatedId  关联业务 ID
     */
    private void insertLog(Long userId, int changeType, String relatedId) {
        QuotaLog logEntry = new QuotaLog();
        logEntry.setUserId(userId);
        logEntry.setChangeType(changeType);
        logEntry.setRelatedId(relatedId);
        quotaLogMapper.insert(logEntry);
    }
}
