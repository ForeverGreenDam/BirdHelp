package com.greendam.birdhelp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.greendam.birdhelp.model.entity.MemberPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author ForeverGreenDam
 * @description 针对表【member_plan(会员套餐表)】的数据库操作Mapper
 */
@Mapper
public interface MemberPlanMapper extends BaseMapper<MemberPlan> {
}
