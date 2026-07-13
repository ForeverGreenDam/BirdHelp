package com.greendam.birdhelp.service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 支付宝支付服务接口，提供电脑网站支付创建和异步回调验签功能。
 * </p>
 *
 * @author ForeverGreenDam
 */
public interface AlipayService {

    /**
     * 创建支付宝电脑网站支付，返回支付宝表单 HTML 片段。
     *
     * @param orderNo 订单号
     * @param amount  支付金额，单位：元
     * @param subject 商品标题
     * @return 支付宝表单 HTML，前端自动提交
     */
    String createPagePay(String orderNo, BigDecimal amount, String subject);

    /**
     * 验证支付宝异步回调签名并解析参数。
     *
     * @param request HttpServletRequest，包含支付宝回调参数
     * @return 验签后的参数 Map
     * @throws RuntimeException 验签失败时抛出
     */
    Map<String, String> parseNotify(HttpServletRequest request);
}
