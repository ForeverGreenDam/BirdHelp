package com.greendam.birdhelp.common.utils;

import com.greendam.birdhelp.properties.MailProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

/**
 * 通用邮件发送工具类。
 *
 * @author ForeverGreenDam
 */
@Data
@AllArgsConstructor
@Slf4j
public class MailUtil {

    private JavaMailSender mailSender;
    private MailProperties mailProperties;

    /**
     * 判断字符串是否为邮箱地址。
     */
    public static boolean isEmail(String target) {
        return target != null && target.contains("@") && target.matches("^[\\w.\\-+]+@[\\w\\-]+\\.[\\w.]+$");
    }

    /**
     * 发送纯文本邮件。
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content 纯文本内容
     */
    public void sendText(String to, String subject, String content) {
        send(to, subject, content, false);
    }

    /**
     * 发送 HTML 邮件。
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param content HTML 内容
     */
    public void sendHtml(String to, String subject, String content) {
        send(to, subject, content, true);
    }

    /**
     * 发送验证码邮件（内置 HTML 模板）。
     *
     * @param to   收件人邮箱
     * @param code 验证码
     * @param type 验证码用途（register / login / reset）
     */
    public void sendVerifyCode(String to, String code, String type) {
        String typeText = switch (type) {
            case "register" -> "注册";
            case "login" -> "登录";
            case "reset" -> "重置密码";
            default -> "验证";
        };

        String subject = "BirdHelp - " + typeText + "验证码";
        String content = buildVerifyCodeHtml(code, typeText);
        sendHtml(to, subject, content);
        log.info("验证码邮件已发送至 {} (类型: {})", to, type);
    }

    /**
     * 构建验证码 HTML 邮件模板。
     */
    private String buildVerifyCodeHtml(String code, String typeText) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset=\"UTF-8\"></head>" +
                "<body style=\"font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;\">" +
                "<div style=\"max-width: 500px; margin: 40px auto; background: #fff; border-radius: 8px; " +
                "box-shadow: 0 2px 8px rgba(0,0,0,0.1); overflow: hidden;\">" +
                "<div style=\"background: #4A90D9; padding: 24px; text-align: center;\">" +
                "<h1 style=\"color: #fff; margin: 0; font-size: 22px;\">BirdHelp</h1>" +
                "</div>" +
                "<div style=\"padding: 32px 24px; text-align: center;\">" +
                "<p style=\"color: #555; font-size: 15px; margin: 0 0 24px;\">" +
                "您正在使用 <b>" + typeText + "</b> 功能，验证码如下：</p>" +
                "<div style=\"background: #f0f6ff; border: 1px dashed #4A90D9; border-radius: 6px; " +
                "padding: 16px; margin: 0 0 24px;\">" +
                "<span style=\"font-size: 28px; font-weight: bold; color: #4A90D9; letter-spacing: 8px;\">" +
                code + "</span>" +
                "</div>" +
                "<p style=\"color: #999; font-size: 13px;\">验证码 5 分钟内有效，请勿泄露给他人。</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 发送邮件（内部统一入口）。
     */
    private void send(String to, String subject, String content, boolean html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, html);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("邮件发送失败 -> {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}
