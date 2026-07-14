package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.common.utils.ThrowUtils;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.MemberOrderMapper;
import com.greendam.birdhelp.mapper.MemberPlanMapper;
import com.greendam.birdhelp.mapper.QuotaConfigMapper;
import com.greendam.birdhelp.mapper.UserQuotaMapper;
import com.greendam.birdhelp.model.entity.MemberOrder;
import com.greendam.birdhelp.model.entity.MemberPlan;
import com.greendam.birdhelp.model.entity.QuotaConfig;
import com.greendam.birdhelp.model.entity.UserQuota;
import com.greendam.birdhelp.model.vo.MemberOrderVO;
import com.greendam.birdhelp.model.vo.MemberStatusVO;
import com.greendam.birdhelp.service.AlipayService;
import com.greendam.birdhelp.service.MemberOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 会员订单服务实现类，核心业务逻辑。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class MemberOrderServiceImpl extends ServiceImpl<MemberOrderMapper, MemberOrder> implements MemberOrderService {

    /**
     * 订单过期时间（分钟）。
     */
    private static final int ORDER_EXPIRE_MINUTES = 15;
    /**
     * 支付宝商品标题前缀。
     */
    private static final String SUBJECT_PREFIX = "BirdHelp会员-";
    @Resource
    private MemberPlanMapper memberPlanMapper;
    @Resource
    private QuotaConfigMapper quotaConfigMapper;
    @Resource
    private UserQuotaMapper userQuotaMapper;
    @Resource
    private AlipayService alipayService;

    // ==================== 公开方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(Long userId, Long planId) {
        // 1. 校验套餐
        MemberPlan plan = memberPlanMapper.selectById(planId);
        ThrowUtils.throwIf(plan == null, ErrorCode.PLAN_NOT_FOUND);
        ThrowUtils.throwIf(plan.getStatus() != 1, ErrorCode.PLAN_DISABLED);

        // 2. 生成订单号（时间戳 + 用户ID后4位 + 随机数）
        String orderNo = generateOrderNo(userId);

        // 3. 创建订单
        MemberOrder order = new MemberOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPlanId(planId);
        order.setAmount(plan.getActualPrice());
        order.setPayType(2); // 支付宝
        order.setStatus(0);  // 待支付
        order.setExpireAt(LocalDateTime.now().plusMinutes(ORDER_EXPIRE_MINUTES));

        baseMapper.insert(order);
        log.info("创建订单成功，订单号：{}，用户ID：{}，套餐：{}", orderNo, userId, plan.getName());

        // 4. 调用支付宝生成支付表单
        String subject = SUBJECT_PREFIX + plan.getName();
        return alipayService.createPagePay(orderNo, plan.getActualPrice(), subject);
    }

    @Override
    public MemberOrderVO getOrderDetail(String orderNo, Long userId) {
        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberOrder::getOrderNo, orderNo);
        MemberOrder order = baseMapper.selectOne(wrapper);
        ThrowUtils.throwIf(order == null, ErrorCode.ORDER_NOT_FOUND);
        ThrowUtils.throwIf(!order.getUserId().equals(userId), ErrorCode.ORDER_NOT_YOURS);

        return toVO(order);
    }

    @Override
    public Page<MemberOrderVO> listOrders(int page, int size, Long userId) {
        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberOrder::getUserId, userId)
                .orderByDesc(MemberOrder::getCreateTime);

        Page<MemberOrder> orderPage = baseMapper.selectPage(new Page<>(page, size), wrapper);

        // 转换为 VO
        Page<MemberOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<MemberOrderVO> voList = orderPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAlipayNotify(Map<String, String> params) {
        // 1. 提取关键参数
        String orderNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");

        log.info("收到支付宝回调，订单号：{}，交易号：{}，状态：{}", orderNo, tradeNo, tradeStatus);

        // 2. 判断交易状态
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.info("交易状态非成功，忽略：{}", tradeStatus);
            return;
        }

        // 3. 查询订单
        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberOrder::getOrderNo, orderNo);
        MemberOrder order = baseMapper.selectOne(wrapper);

        if (order == null) {
            log.error("订单不存在：{}", orderNo);
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 4. 幂等性校验：已支付的订单直接返回
        if (order.getStatus() == 1) {
            log.info("订单已支付，忽略重复回调：{}", orderNo);
            return;
        }

        // 5. 校验订单状态
        if (order.getStatus() == 2) {
            log.error("订单已过期：{}", orderNo);
            throw new BusinessException(ErrorCode.ORDER_EXPIRED);
        }

        // 6. 更新订单状态
        order.setStatus(1); // 已支付
        order.setTradeNo(tradeNo);
        order.setPaidAt(LocalDateTime.now());
        baseMapper.updateById(order);

        // 7. 激活会员
        activateMember(order.getUserId(), order.getPlanId());

        log.info("订单支付成功，会员已激活，订单号：{}", orderNo);
    }

    @Override
    public int checkAndExpireOrders() {
        LambdaUpdateWrapper<MemberOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(MemberOrder::getStatus, 0) // 待支付
                .lt(MemberOrder::getExpireAt, LocalDateTime.now()) // 已过期
                .set(MemberOrder::getStatus, 2); // 标记为已过期

        int count = baseMapper.update(null, wrapper);
        if (count > 0) {
            log.info("订单过期检查完成，过期订单数：{}", count);
        }
        return count;
    }

    @Override
    public MemberStatusVO getMemberStatus(Long userId) {
        // 查询用户额度信息
        LambdaQueryWrapper<UserQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuota::getUserId, userId);
        UserQuota quota = userQuotaMapper.selectOne(wrapper);

        // 默认免费用户
        Integer memberLevel = 0;
        LocalDateTime memberExpireAt = null;
        Integer dailyLimit = 10; // 免费用户默认10次

        if (quota != null) {
            memberLevel = quota.getMemberLevel() != null ? quota.getMemberLevel() : 0;
            memberExpireAt = quota.getMemberExpireAt();

            // 检查是否过期
            if (memberExpireAt != null && memberExpireAt.isBefore(LocalDateTime.now())) {
                memberLevel = 0;
                memberExpireAt = null;
            }

            // 从额度配置查询对应等级的每日限额
            QuotaConfig quotaConfig = quotaConfigMapper.selectOne(
                    new LambdaQueryWrapper<QuotaConfig>().eq(QuotaConfig::getLevel, memberLevel)
            );
            if (quotaConfig != null) {
                dailyLimit = quotaConfig.getDailyLimit();
            }
        }

        // 构建等级名称
        String memberLevelName = getMemberLevelName(memberLevel);

        return MemberStatusVO.builder()
                .memberLevel(memberLevel)
                .memberLevelName(memberLevelName)
                .memberExpireAt(memberExpireAt)
                .isExpired(memberExpireAt != null && memberExpireAt.isBefore(LocalDateTime.now()))
                .dailyLimit(dailyLimit)
                .build();
    }

    // ==================== 内部方法 ====================

    /**
     * 激活会员。
     */
    private void activateMember(Long userId, Long planId) {
        MemberPlan plan = memberPlanMapper.selectById(planId);
        if (plan == null) {
            log.error("激活会员失败，套餐不存在：{}", planId);
            return;
        }

        // 从额度配置获取有效天数
        QuotaConfig quotaConfig = quotaConfigMapper.selectOne(
                new LambdaQueryWrapper<QuotaConfig>().eq(QuotaConfig::getLevel, plan.getLevel())
        );
        if (quotaConfig == null) {
            log.error("激活会员失败，额度配置不存在：level={}", plan.getLevel());
            return;
        }

        // 查询或创建用户额度记录
        LambdaQueryWrapper<UserQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserQuota::getUserId, userId);
        UserQuota quota = userQuotaMapper.selectOne(wrapper);

        if (quota == null) {
            quota = new UserQuota();
            quota.setUserId(userId);
            quota.setMemberLevel(0);
            quota.setDailyUsed(0);
            userQuotaMapper.insert(quota);
        }

        // 计算新的到期时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime newExpireAt;

        if (quota.getMemberExpireAt() != null && quota.getMemberExpireAt().isAfter(now)) {
            // 当前会员未过期，在现有到期时间上追加
            newExpireAt = quota.getMemberExpireAt().plusDays(quotaConfig.getDurationDays());
        } else {
            // 已过期或无会员，从现在开始计算
            newExpireAt = now.plusDays(quotaConfig.getDurationDays());
        }

        // 更新用户额度
        quota.setMemberLevel(plan.getLevel());
        quota.setMemberExpireAt(newExpireAt);
        userQuotaMapper.updateById(quota);

        log.info("会员激活成功，用户ID：{}，等级：{}，到期时间：{}", userId, plan.getLevel(), newExpireAt);
    }

    /**
     * 生成订单号。
     */
    private String generateOrderNo(Long userId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String userIdSuffix = String.format("%04d", userId % 10000);
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return timestamp + userIdSuffix + random;
    }

    /**
     * 获取会员等级名称。
     */
    private String getMemberLevelName(Integer level) {
        if (level == null || level == 0) {
            return "免费用户";
        }
        switch (level) {
            case 1:
                return "月卡";
            case 2:
                return "季卡";
            case 3:
                return "年卡";
            default:
                return "免费用户";
        }
    }

    /**
     * 实体转 VO。
     */
    private MemberOrderVO toVO(MemberOrder order) {
        // 查询套餐名称
        String planName = "";
        if (order.getPlanId() != null) {
            MemberPlan plan = memberPlanMapper.selectById(order.getPlanId());
            if (plan != null) {
                planName = plan.getName();
            }
        }

        return MemberOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .planId(order.getPlanId())
                .planName(planName)
                .amount(order.getAmount())
                .payType(order.getPayType())
                .status(order.getStatus())
                .tradeNo(order.getTradeNo())
                .paidAt(order.getPaidAt())
                .expireAt(order.getExpireAt())
                .createTime(order.getCreateTime())
                .build();
    }
}
