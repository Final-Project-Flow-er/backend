package com.chaing.api.facade.user.listener;

import com.chaing.api.dto.user.event.PasswordResetEvent;
import com.chaing.api.dto.user.event.ProfileImageDeleteEvent;
import com.chaing.api.dto.user.event.ProfileImageUploadEvent;
import com.chaing.api.dto.user.event.UserInfoResendEvent;
import com.chaing.api.dto.user.event.UserRegisteredEvent;
import com.chaing.core.enums.BucketName;
import com.chaing.core.service.MinioService;
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
    private final MinioService minioService;

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

    // 회원 정보 재발송 메일 발송
    @Async("mailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserInfoResendEvent(UserInfoResendEvent event) {
        mailService.sendUserInfo(
                event.email(),
                event.loginId(),
                event.employeeNumber()
        );
    }

    // 프로필 이미지 업로드
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileUpload(ProfileImageUploadEvent event) {
        minioService.uploadFile(event.file(), event.fileName(), BucketName.PROFILES);
    }

    // 프로필 이미지 삭제
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFileDelete(ProfileImageDeleteEvent event) {
        minioService.deleteFile(event.fileName(), event.bucketName());
    }
}
