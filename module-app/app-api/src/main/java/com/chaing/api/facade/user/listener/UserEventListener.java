package com.chaing.api.facade.user.listener;

import com.chaing.api.dto.user.event.PasswordResetEvent;
import com.chaing.api.dto.user.event.UserRegisteredEvent;
import com.chaing.domain.users.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final MailService mailService;

    // 회원가입 메일 발송
    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        mailService.sendRegisterMail(
                event.email(),
                event.loginId(),
                event.tempPassword(),
                event.employeeNumber()
        );
    }

    // 비밀번호 재설정 메일 발송
    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetEvent(PasswordResetEvent event) {
        mailService.sendTempPassword(
                event.email(),
                event.tempPassword()
        );
    }
}
