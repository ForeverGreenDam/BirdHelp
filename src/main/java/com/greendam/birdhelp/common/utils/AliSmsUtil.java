package com.greendam.birdhelp.common.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.greendam.birdhelp.properties.AliSmsProperties;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * 阿里云短信服务工具类
 *
 * @author ForeverGreenDam
 */
@Slf4j
public class AliSmsUtil {

    @Resource
    private AliSmsProperties smsProperties;

    /**
     * 发送短信验证码
     *
     * @param phoneNumber 手机号（11位）
     * @param code        6位验证码
     * @return 是否发送成功
     */
    public boolean sendVerifyCode(String phoneNumber, String code) {
        try {
            // 创建客户端
            Client client = createClient();

            // 构建请求
            SendSmsRequest sendSmsRequest = new SendSmsRequest()
                    .setPhoneNumbers(phoneNumber)
                    .setSignName(smsProperties.getSignName())
                    .setTemplateCode(smsProperties.getTemplateCode())
                    .setTemplateParam("{\"code\":\"" + code + "\"}");

            // 发送短信
            SendSmsResponse response = client.sendSms(sendSmsRequest);

            // 判断发送结果
            if ("OK".equals(response.getBody().getCode())) {
                log.info("短信发送成功: 手机号={}, MessageId={}", phoneNumber, response.getBody().getBizId());
                return true;
            } else {
                log.error("短信发送失败: 手机号={}, Code={}, Message={}",
                        phoneNumber, response.getBody().getCode(), response.getBody().getMessage());
                return false;
            }
        } catch (Exception e) {
            log.error("短信发送异常: 手机号={}", phoneNumber, e);
            return false;
        }
    }

    /**
     * 创建阿里云短信客户端
     *
     * @return Client 实例
     * @throws Exception 创建异常
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(smsProperties.getAccessKeyId())
                .setAccessKeySecret(smsProperties.getAccessKeySecret())
                .setEndpoint("dysmsapi.aliyuncs.com");
        return new Client(config);
    }

}
