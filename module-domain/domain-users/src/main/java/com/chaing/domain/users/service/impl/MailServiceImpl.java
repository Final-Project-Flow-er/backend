package com.chaing.domain.users.service.impl;

import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.service.MailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
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

    // 1. 회원 등록 메일 (아이디, 비번, 사번 포함)
    @Override
    public void sendRegisterMail(String email, String loginId, String tempPassword, String employeeNumber) {
        Context context = new Context();
        context.setVariable("loginId", loginId);
        context.setVariable("tempPassword", tempPassword);
        context.setVariable("employeeNumber", employeeNumber);

        String htmlContent = templateEngine.process("mail/register", context);
        sendHtmlMail(email, "[CHAING] 회원 등록을 축하합니다.", htmlContent);
    }

    // 2. 임시 비밀번호 메일 (비번만 포함)
    @Override
    public void sendTempPassword(String email, String tempPassword) {
        Context context = new Context();
        context.setVariable("tempPassword", tempPassword);

        String htmlContent = templateEngine.process("mail/temp-password", context);
        sendHtmlMail(email, "[CHAING] 임시 비밀번호 발급 안내", htmlContent);
    }

    // 메일 발송 공통 로직 (중복 제거를 위해 분리)
    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            // 이미지를 포함해야 하므로 true(멀티파트) 설정 필수
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            // 프로젝트 내의 static/images/logo.png 파일을 읽어옵니다.
            // (파일이 해당 경로에 실제로 존재해야 합니다!)
            ClassPathResource res = new ClassPathResource("static/images/logo.png");

            // HTML의 <img src="cid:logo"> 부분과 매칭됩니다.
            helper.addInline("logo", res);

            mailSender.send(message);
        } catch (Exception e) {
            throw new UserException(UserErrorCode.MAIL_SEND_FAILED);
        }
    }
}
