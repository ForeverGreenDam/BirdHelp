package com.greendam.birdhelp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.greendam.birdhelp.common.BaseResponse;
import com.greendam.birdhelp.common.context.BaseContext;
import com.greendam.birdhelp.model.dto.CreateOrderDTO;
import com.greendam.birdhelp.model.vo.MemberOrderVO;
import com.greendam.birdhelp.model.vo.MemberPlanVO;
import com.greendam.birdhelp.model.vo.MemberStatusVO;
import com.greendam.birdhelp.service.MemberOrderService;
import com.greendam.birdhelp.service.MemberPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 会员模块用户端控制器，提供套餐查询、订单管理、会员状态查询等接口。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberPlanService memberPlanService;

    @Resource
    private MemberOrderService memberOrderService;

    /**
     * 查询套餐列表（所有上架套餐）。
     *
     * @return 套餐列表
     */
    @GetMapping("/plans")
    public BaseResponse<List<MemberPlanVO>> listPlans() {
        List<MemberPlanVO> plans = memberPlanService.listActivePlans();
        return BaseResponse.success(plans);
    }

    /**
     * 创建订单并返回支付宝支付表单。
     *
     * @param dto      创建订单请求体
     * @param response HttpServletResponse，用于直接输出表单 HTML
     */
    @PostMapping("/orders")
    public void createOrder(@Valid @RequestBody CreateOrderDTO dto, HttpServletResponse response) throws IOException {
        Long userId = BaseContext.getCurrentId();
        String payForm = memberOrderService.createOrder(userId, dto.getPlanId());

        // 直接输出支付宝表单 HTML，前端自动提交
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(payForm);
        response.getWriter().flush();
    }

    /**
     * 查询订单详情。
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    @GetMapping("/orders/{orderNo}")
    public BaseResponse<MemberOrderVO> getOrderDetail(@PathVariable String orderNo) {
        Long userId = BaseContext.getCurrentId();
        MemberOrderVO order = memberOrderService.getOrderDetail(orderNo, userId);
        return BaseResponse.success(order);
    }

    /**
     * 查询我的订单列表（分页）。
     *
     * @param page 页码，默认 1
     * @param size 每页数量，默认 10
     * @return 分页订单列表
     */
    @GetMapping("/orders")
    public BaseResponse<Page<MemberOrderVO>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = BaseContext.getCurrentId();
        Page<MemberOrderVO> orders = memberOrderService.listOrders(page, size, userId);
        return BaseResponse.success(orders);
    }

    /**
     * 查询当前会员状态。
     *
     * @return 会员状态
     */
    @GetMapping("/status")
    public BaseResponse<MemberStatusVO> getMemberStatus() {
        Long userId = BaseContext.getCurrentId();
        MemberStatusVO status = memberOrderService.getMemberStatus(userId);
        return BaseResponse.success(status);
    }
}
