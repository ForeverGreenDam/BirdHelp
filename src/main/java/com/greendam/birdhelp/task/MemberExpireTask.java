package com.greendam.birdhelp.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.greendam.birdhelp.mapper.UserQuotaMapper;
import com.greendam.birdhelp.model.entity.UserQuota;
import com.greendam.birdhelp.service.MemberOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 会员相关定时任务，包括订单过期检查和会员过期回退。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Component
public class MemberExpireTask {

    @Resource
    private MemberOrderService memberOrderService;

    @Resource
    private UserQuotaMapper userQuotaMapper;

    /**
     * 每5分钟检查并过期未支付的订单。
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkAndExpireOrders() {
        log.info("开始执行订单过期检查");
        int count = memberOrderService.checkAndExpireOrders();
        if (count > 0) {
            log.info("订单过期检查完成，过期订单数：{}", count);
        }
    }

    /**
     * 每小时检查并回退已过期的会员。
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkAndRevertExpiredMembers() {
        log.info("开始执行会员过期检查");

        LambdaUpdateWrapper<UserQuota> wrapper = new LambdaUpdateWrapper<>();
        wrapper.isNotNull(UserQuota::getMemberExpireAt)
                .lt(UserQuota::getMemberExpireAt, LocalDateTime.now())
                .gt(UserQuota::getMemberLevel, 0)
                .set(UserQuota::getMemberLevel, 0)
                .set(UserQuota::getMemberExpireAt, null);

        int count = userQuotaMapper.update(null, wrapper);
        if (count > 0) {
            log.info("会员过期回退完成，回退用户数：{}", count);
        }
    }
}
