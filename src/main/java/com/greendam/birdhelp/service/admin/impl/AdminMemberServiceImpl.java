package com.greendam.birdhelp.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.utils.ThrowUtils;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.MemberOrderMapper;
import com.greendam.birdhelp.mapper.MemberPlanMapper;
import com.greendam.birdhelp.mapper.SysUserMapper;
import com.greendam.birdhelp.model.dto.admin.MemberPlanCreateDTO;
import com.greendam.birdhelp.model.dto.admin.MemberPlanUpdateDTO;
import com.greendam.birdhelp.model.entity.MemberOrder;
import com.greendam.birdhelp.model.entity.MemberPlan;
import com.greendam.birdhelp.model.entity.SysUser;
import com.greendam.birdhelp.model.vo.admin.AdminMemberOrderVO;
import com.greendam.birdhelp.service.admin.AdminMemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 管理员会员服务实现类。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class AdminMemberServiceImpl implements AdminMemberService {

    @Resource
    private MemberPlanMapper memberPlanMapper;

    @Resource
    private MemberOrderMapper memberOrderMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    // ==================== 套餐管理 ====================

    @Override
    public List<MemberPlan> listPlans() {
        LambdaQueryWrapper<MemberPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(MemberPlan::getActualPrice);
        return memberPlanMapper.selectList(wrapper);
    }

    @Override
    public MemberPlan getPlanDetail(Long planId) {
        MemberPlan plan = memberPlanMapper.selectById(planId);
        ThrowUtils.throwIf(plan == null, ErrorCode.PLAN_NOT_FOUND);
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPlan(MemberPlanCreateDTO dto) {
        // 检查等级是否已存在
        LambdaQueryWrapper<MemberPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberPlan::getLevel, dto.getLevel());
        Long count = memberPlanMapper.selectCount(wrapper);
        ThrowUtils.throwIf(count > 0, new BusinessException(ErrorCode.PARAMS_ERROR, "该等级套餐已存在"));

        MemberPlan plan = new MemberPlan();
        plan.setName(dto.getName());
        plan.setLevel(dto.getLevel());
        plan.setPrice(dto.getPrice());
        plan.setActualPrice(dto.getActualPrice());
        plan.setStatus(1); // 默认上架

        memberPlanMapper.insert(plan);
        log.info("新增套餐成功，套餐：{}，等级：{}", plan.getName(), plan.getLevel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlan(MemberPlanUpdateDTO dto) {
        MemberPlan plan = memberPlanMapper.selectById(dto.getId());
        ThrowUtils.throwIf(plan == null, ErrorCode.PLAN_NOT_FOUND);

        if (dto.getName() != null) {
            plan.setName(dto.getName());
        }
        if (dto.getPrice() != null) {
            plan.setPrice(dto.getPrice());
        }
        if (dto.getActualPrice() != null) {
            plan.setActualPrice(dto.getActualPrice());
        }

        memberPlanMapper.updateById(plan);
        log.info("修改套餐成功，套餐ID：{}", plan.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePlanStatus(Long planId, Integer status) {
        MemberPlan plan = memberPlanMapper.selectById(planId);
        ThrowUtils.throwIf(plan == null, ErrorCode.PLAN_NOT_FOUND);

        plan.setStatus(status);
        memberPlanMapper.updateById(plan);
        log.info("套餐状态更新成功，套餐ID：{}，状态：{}", planId, status);
    }

    // ==================== 订单管理 ====================

    @Override
    public Page<AdminMemberOrderVO> listOrders(int page, int size, Long userId, Integer status) {
        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(MemberOrder::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(MemberOrder::getStatus, status);
        }
        wrapper.orderByDesc(MemberOrder::getCreateTime);

        Page<MemberOrder> orderPage = memberOrderMapper.selectPage(new Page<>(page, size), wrapper);

        // 转换为 VO
        Page<AdminMemberOrderVO> voPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<AdminMemberOrderVO> voList = orderPage.getRecords().stream()
                .map(this::toAdminOrderVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AdminMemberOrderVO getOrderDetail(String orderNo) {
        LambdaQueryWrapper<MemberOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberOrder::getOrderNo, orderNo);
        MemberOrder order = memberOrderMapper.selectOne(wrapper);
        ThrowUtils.throwIf(order == null, ErrorCode.ORDER_NOT_FOUND);
        return toAdminOrderVO(order);
    }

    // ==================== 内部方法 ====================

    /**
     * 订单实体转管理员 VO。
     */
    private AdminMemberOrderVO toAdminOrderVO(MemberOrder order) {
        // 查询用户信息
        String username = "";
        String nickname = "";
        if (order.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(order.getUserId());
            if (user != null) {
                username = user.getUsername();
                nickname = user.getNickname();
            }
        }

        // 查询套餐名称
        String planName = "";
        if (order.getPlanId() != null) {
            MemberPlan plan = memberPlanMapper.selectById(order.getPlanId());
            if (plan != null) {
                planName = plan.getName();
            }
        }

        return AdminMemberOrderVO.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .username(username)
                .nickname(nickname)
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
