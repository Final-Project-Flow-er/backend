package com.chaing.domain.users.service.impl;

import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.service.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.url.login}")
    private String loginUrl;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.mail.from-name:CHAIN-G}")
    private String fromName;

    // 회원 등록 메일
    @Override
    public void sendRegisterMail(String email, String loginId, String tempPassword, String employeeNumber) {
        Context context = new Context();
        context.setVariable("loginId", loginId);
        context.setVariable("tempPassword", tempPassword);
        context.setVariable("employeeNumber", employeeNumber);
        context.setVariable("loginUrl", loginUrl);

        String htmlContent = templateEngine.process("mail/register", context);
        sendHtmlMail(email, "[CHAIN-G] 회원 등록을 축하합니다.", htmlContent);
    }

    // 임시 비밀번호 메일
    @Override
    public void sendTempPassword(String email, String tempPassword) {
        Context context = new Context();
        context.setVariable("tempPassword", tempPassword);
        context.setVariable("loginUrl", loginUrl);

        String htmlContent = templateEngine.process("mail/temp-password", context);
        sendHtmlMail(email, "[CHAIN-G] 임시 비밀번호 발급 안내", htmlContent);
    }

    // 회원 정보 재발송 메일 (비밀번호 제외)
    @Override
    public void sendUserInfo(String email, String loginId, String employeeNumber) {
        Context context = new Context();
        context.setVariable("loginId", loginId);
        context.setVariable("employeeNumber", employeeNumber);
        context.setVariable("loginUrl", loginUrl);

        String htmlContent = templateEngine.process("mail/user-info", context);
        sendHtmlMail(email, "[CHAIN-G] 회원 계정 정보 안내", htmlContent);
    }

    // 메일 발송 공통 로직
    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new jakarta.mail.internet.InternetAddress(fromAddress, fromName, "UTF-8"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            ClassPathResource res = new ClassPathResource("static/images/logo.png");

            helper.addInline("logo", res);

            mailSender.send(message);
        } catch (Exception e) {
            throw new UserException(UserErrorCode.MAIL_SEND_FAILED);
        }
    }
}
