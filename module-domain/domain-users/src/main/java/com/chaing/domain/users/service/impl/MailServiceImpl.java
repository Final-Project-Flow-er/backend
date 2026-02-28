package com.chaing.domain.users.service.impl;

import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    // 이메일로 회원 등록 정보 전송
    @Override
    public void sendRegisterMail(String email, String loginId, String tempPassword, String employeeNumber) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[CHAING] 회원 등록을 축하합니다.");
        message.setText("안녕하세요, CHAING 서비스입니다.\n\n" +
                "로그인 아이디는 다음과 같습니다.\n" +
                "아이디: " + loginId + "\n\n" +
                "임시 비밀번호는 다음과 같습니다.\n" +
                "임시 비밀번호: " + tempPassword + "\n\n" +
                "사원번호는 다음과 같습니다.\n" +
                "사원번호: " + employeeNumber + "\n\n" +
                "로그인 후 마이페이지에서 반드시 비밀번호를 변경해 주세요.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new UserException(UserErrorCode.MAIL_SEND_FAILED);
        }
    }

    // 이메일로 임시 비밀번호 전송
    @Override
    public void sendTempPassword(String email, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[CHAING] 임시 비밀번호 발급 안내");
        message.setText("안녕하세요, CHAING 서비스입니다.\n\n" +
                "요청하신 임시 비밀번호는 다음과 같습니다.\n" +
                "임시 비밀번호: " + tempPassword + "\n\n" +
                "로그인 후 마이페이지에서 반드시 비밀번호를 변경해 주세요.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new UserException(UserErrorCode.MAIL_SEND_FAILED);
        }
    }
}
