package com.greendam.birdhelp.controller;

import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.service.AlipayService;
import com.greendam.birdhelp.service.MemberOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * 支付宝异步回调控制器，接收支付宝支付结果通知。
 * 此接口无需 JWT 鉴权，由支付宝服务器直接调用。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@RestController
@RequestMapping("/pay/alipay")
public class AlipayNotifyController {

    @Resource
    private AlipayService alipayService;

    @Resource
    private MemberOrderService memberOrderService;

    /**
     * 支付宝异步回调接口。
     * <p>
     * 支付宝会在用户支付成功后主动调用此接口通知支付结果。
     * 必须返回 "success" 字符串，否则支付宝会重复通知。
     * </p>
     *
     * @param request HttpServletRequest，包含支付宝回调参数
     * @return "success" 表示处理成功，其他表示失败
     */
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            // 1. 验签并解析参数
            Map<String, String> params = alipayService.parseNotify(request);

            // 2. 处理支付结果
            memberOrderService.handleAlipayNotify(params);

            return "success";
        } catch (BusinessException e) {
            log.error("支付宝回调处理失败：{}", e.getMessage());
            return "failure";
        } catch (Exception e) {
            log.error("支付宝回调处理异常", e);
            return "failure";
        }
    }

    /**
     * 支付宝同步跳转接口。
     * <p>
     * 用户支付完成后，浏览器会跳转到此接口，然后重定向到前端支付结果页面。
     * 前端需设计支付结果页面（如 /pay/result），展示支付状态。
     * </p>
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse，用于发送重定向
     */
    @GetMapping("/return")
    public void returnUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 获取订单号
        String orderNo = request.getParameter("out_trade_no");

        // 重定向到前端支付结果页面，通过 query 参数传递订单号
        // 前端路由示例：http://your-domain.com/pay/result?orderNo=xxx
        //todo: 这里的前端地址需要根据实际部署情况修改
        String frontendBaseUrl = "https://forevergreendam.cn";
        String redirectUrl = frontendBaseUrl + "/pay/result?orderNo=" + orderNo;

        response.sendRedirect(redirectUrl);
    }
}
