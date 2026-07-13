package com.greendam.birdhelp.service.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.model.dto.admin.MemberPlanCreateDTO;
import com.greendam.birdhelp.model.dto.admin.MemberPlanUpdateDTO;
import com.greendam.birdhelp.model.entity.MemberPlan;
import com.greendam.birdhelp.model.vo.admin.AdminMemberOrderVO;

/**
 * <p>
 * 管理员会员服务接口，提供套餐管理、订单查询、手动授予会员等功能。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface AdminMemberService {

    /**
     * 查询所有套餐列表。
     *
     * @return 套餐列表
     */
    java.util.List<MemberPlan> listPlans();

    /**
     * 查询套餐详情。
     *
     * @param planId 套餐 ID
     * @return 套餐详情
     */
    MemberPlan getPlanDetail(Long planId);

    /**
     * 新增套餐。
     *
     * @param dto 创建套餐参数
     */
    void createPlan(MemberPlanCreateDTO dto);

    /**
     * 修改套餐。
     *
     * @param dto 修改套餐参数
     */
    void updatePlan(MemberPlanUpdateDTO dto);

    /**
     * 上架/下架套餐。
     *
     * @param planId 套餐 ID
     * @param status 状态：0-下架 1-上架
     */
    void updatePlanStatus(Long planId, Integer status);

    /**
     * 查询订单列表（支持筛选）。
     *
     * @param page   页码
     * @param size   每页数量
     * @param userId 用户 ID（可选）
     * @param status 订单状态（可选）
     * @return 分页订单列表
     */
    Page<AdminMemberOrderVO> listOrders(int page, int size, Long userId, Integer status);

    /**
     * 查询订单详情。
     *
     * @param orderId 订单 ID
     * @return 订单详情
     */
    AdminMemberOrderVO getOrderDetail(Long orderId);
}
