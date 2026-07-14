package com.greendam.birdhelp.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.properties.AlipayProperties;
import com.greendam.birdhelp.service.AlipayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 支付宝支付服务实现类，封装支付宝 SDK 调用逻辑。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class AlipayServiceImpl implements AlipayService {

    @Resource
    private AlipayProperties alipayProperties;

    private volatile AlipayClient alipayClient;

    /**
     * 获取或初始化 AlipayClient 单例。
     */
    private AlipayClient getAlipayClient() {
        if (alipayClient == null) {
            synchronized (AlipayServiceImpl.class) {
                if (alipayClient == null) {
                    log.info("支付宝配置: appId={}, privateKey长度={}, alipayPublicKey长度={}",
                            alipayProperties.getAppId(),
                            alipayProperties.getPrivateKey() != null ? alipayProperties.getPrivateKey().length() : 0,
                            alipayProperties.getAlipayPublicKey() != null ? alipayProperties.getAlipayPublicKey().length() : 0);
                    alipayClient = new DefaultAlipayClient(
                            alipayProperties.getGatewayUrl(),
                            alipayProperties.getAppId(),
                            alipayProperties.getPrivateKey(),
                            "json",
                            "UTF-8",
                            alipayProperties.getAlipayPublicKey(),
                            alipayProperties.getSignType()
                    );
                }
            }
        }
        return alipayClient;
    }

    @Override
    public String createPagePay(String orderNo, BigDecimal amount, String subject) {
        try {
            AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
            payRequest.setNotifyUrl(alipayProperties.getNotifyUrl());
            payRequest.setReturnUrl(alipayProperties.getReturnUrl());

            // 构建业务参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("total_amount", amount.toPlainString());
            bizContent.put("subject", subject);
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

            payRequest.setBizContent(toJson(bizContent));

            // 调用 SDK 获取表单 HTML
            return getAlipayClient().pageExecute(payRequest).getBody();
        } catch (AlipayApiException e) {
            log.error("创建支付宝支付失败，订单号：{}", orderNo, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建支付失败");
        }
    }

    @Override
    public Map<String, String> parseNotify(HttpServletRequest request) {
        try {
            // 从 request 中提取所有参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                StringBuilder valueStr = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    valueStr.append(i == values.length - 1 ? values[i] : values[i] + ",");
                }
                params.put(name, valueStr.toString());
            }

            // 验签
            boolean verified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );

            if (!verified) {
                log.error("支付宝回调验签失败");
                throw new BusinessException(ErrorCode.ALIPAY_VERIFY_FAILED);
            }

            return params;
        } catch (BusinessException e) {
            throw e;
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
            throw new BusinessException(ErrorCode.ALIPAY_VERIFY_FAILED);
        }
    }

    /**
     * 简易 JSON 序列化，避免引入额外依赖。
     */
    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append("\"").append(value).append("\"");
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
