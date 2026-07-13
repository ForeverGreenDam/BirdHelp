package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.mapper.MemberPlanMapper;
import com.greendam.birdhelp.model.entity.MemberPlan;
import com.greendam.birdhelp.model.vo.MemberPlanVO;
import com.greendam.birdhelp.service.MemberPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 会员套餐服务实现类。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class MemberPlanServiceImpl extends ServiceImpl<MemberPlanMapper, MemberPlan> implements MemberPlanService {

    @Override
    public List<MemberPlanVO> listActivePlans() {
        LambdaQueryWrapper<MemberPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPlan::getStatus, 1)
                .orderByAsc(MemberPlan::getActualPrice);

        List<MemberPlan> plans = list(wrapper);

        return plans.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /**
     * 实体转 VO。
     */
    private MemberPlanVO toVO(MemberPlan plan) {
        return MemberPlanVO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .level(plan.getLevel())
                .price(plan.getPrice())
                .actualPrice(plan.getActualPrice())
                .durationDays(plan.getDurationDays())
                .dailyLimit(plan.getDailyLimit())
                .build();
    }
}
