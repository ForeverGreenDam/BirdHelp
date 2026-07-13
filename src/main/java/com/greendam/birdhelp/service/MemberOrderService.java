package com.greendam.birdhelp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.greendam.birdhelp.model.entity.MemberOrder;
import com.greendam.birdhelp.model.vo.MemberOrderVO;
import com.greendam.birdhelp.model.vo.MemberStatusVO;

import java.util.Map;

/**
 * <p>
 * 会员订单服务接口，提供订单创建、查询、支付回调处理、会员激活等功能。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface MemberOrderService extends IService<MemberOrder> {

    /**
     * 创建订单并返回支付宝支付表单 HTML。
     *
     * @param userId 用户 ID
     * @param planId 套餐 ID
     * @return 支付宝支付表单 HTML
     */
    String createOrder(Long userId, Long planId);

    /**
     * 查询订单详情。
     *
     * @param orderId 订单 ID
     * @param userId  用户 ID（用于权限校验）
     * @return 订单详情
     */
    MemberOrderVO getOrderDetail(Long orderId, Long userId);

    /**
     * 分页查询用户订单列表。
     *
     * @param page   页码
     * @param size   每页数量
     * @param userId 用户 ID
     * @return 分页订单列表
     */
    Page<MemberOrderVO> listOrders(int page, int size, Long userId);

    /**
     * 处理支付宝异步回调。
     *
     * @param params 支付宝回调参数（已验签）
     */
    void handleAlipayNotify(Map<String, String> params);

    /**
     * 检查并过期已超时未支付的订单。
     *
     * @return 处理的订单数量
     */
    int checkAndExpireOrders();

    /**
     * 查询用户会员状态。
     *
     * @param userId 用户 ID
     * @return 会员状态
     */
    MemberStatusVO getMemberStatus(Long userId);
}
