package com.greendam.birdhelp.common.utils;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;


@Data
@Slf4j
public class AliMessageUtil {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;

    public AliMessageUtil(String endpoint, String accessKeyId, String accessKeySecret, String signName, String templateCode) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
    }

    @Resource
    private Client client;

    public SendSmsResponse sendMessage(String phoneNumber, String code) {
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam("{\"code\":\"" + code + "\"}");
        try {
            SendSmsResponse sendSmsResponse = client.sendSms(sendSmsRequest);
            return sendSmsResponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
