package com.chaing.domain.users.service.impl;

import com.chaing.domain.users.exception.UserException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTests {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private MailServiceImpl mailService;

    @Test
    @DisplayName("회원 등록 메일 발송")
    void sendRegisterMail() {

        // given
        String email = "test@example.com";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mail/register"), any(Context.class)))
                .thenReturn("<html>Test Content</html>");

        // when
        mailService.sendRegisterMail(email, "loginId", "tempPassword", "empNo");

        // then
        verify(templateEngine, times(1)).process(eq("mail/register"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("임시 비밀번호 메일 발송")
    void sendTempPassword() {

        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mail/temp-password"), any(Context.class)))
                .thenReturn("<html>Test Content</html>");

        // when
        mailService.sendTempPassword("test@example.com", "tempPassword");

        // then
        verify(templateEngine, times(1)).process(eq("mail/temp-password"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("회원 정보 재발송 메일 발송")
    void sendUserInfo() {

        // given
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("mail/user-info"), any(Context.class)))
                .thenReturn("<html>User Info Content</html>");

        // when
        mailService.sendUserInfo("test@example.com", "loginId", "empNo");

        // then
        verify(templateEngine, times(1)).process(eq("mail/user-info"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("메일 발송 실패 시 UserException 발생")
    void sendMail_Failure_ThrowsException() {
        // given
        String email = "test@example.com";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        lenient().when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>content</html>");

        doThrow(new RuntimeException("SMTP Error")).when(mailSender).send(any(MimeMessage.class));

        // when & then
        assertThrows(UserException.class, () -> mailService.sendTempPassword(email, "tempPassword"));
    }
}