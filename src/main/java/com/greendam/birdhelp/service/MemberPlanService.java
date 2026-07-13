package com.greendam.birdhelp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.model.entity.MemberPlan;
import com.greendam.birdhelp.model.vo.MemberPlanVO;

import java.util.List;

/**
 * <p>
 * 会员套餐服务接口，提供套餐查询功能。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface MemberPlanService extends IService<MemberPlan> {

    /**
     * 查询所有上架套餐，按价格升序排列。
     *
     * @return 上架套餐列表
     */
    List<MemberPlanVO> listActivePlans();
}
