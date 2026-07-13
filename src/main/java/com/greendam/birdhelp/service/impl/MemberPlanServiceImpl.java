package com.greendam.birdhelp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.mapper.MemberPlanMapper;
import com.greendam.birdhelp.mapper.QuotaConfigMapper;
import com.greendam.birdhelp.model.entity.MemberPlan;
import com.greendam.birdhelp.model.entity.QuotaConfig;
import com.greendam.birdhelp.model.vo.MemberPlanVO;
import com.greendam.birdhelp.service.MemberPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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

    @Resource
    private QuotaConfigMapper quotaConfigMapper;

    @Override
    public List<MemberPlanVO> listActivePlans() {
        LambdaQueryWrapper<MemberPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPlan::getStatus, 1)
                .orderByAsc(MemberPlan::getActualPrice);

        List<MemberPlan> plans = list(wrapper);

        // 批量查询关联的额度配置
        List<Integer> levels = plans.stream().map(MemberPlan::getLevel).collect(Collectors.toList());
        List<QuotaConfig> configs = quotaConfigMapper.selectList(
                new LambdaQueryWrapper<QuotaConfig>().in(QuotaConfig::getLevel, levels)
        );
        Map<Integer, QuotaConfig> configMap = configs.stream()
                .collect(Collectors.toMap(QuotaConfig::getLevel, c -> c));

        return plans.stream()
                .map(plan -> toVO(plan, configMap.get(plan.getLevel())))
                .collect(Collectors.toList());
    }

    /**
     * 实体转 VO，关联额度配置填充有效天数和每日上限。
     */
    private MemberPlanVO toVO(MemberPlan plan, QuotaConfig config) {
        Integer durationDays = config != null ? config.getDurationDays() : 0;
        Integer dailyLimit = config != null ? config.getDailyLimit() : 0;

        return MemberPlanVO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .level(plan.getLevel())
                .price(plan.getPrice())
                .actualPrice(plan.getActualPrice())
                .durationDays(durationDays)
                .dailyLimit(dailyLimit)
                .build();
    }
}
