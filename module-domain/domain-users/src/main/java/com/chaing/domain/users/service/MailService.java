package com.chaing.domain.users.service;

public interface MailService {

    void sendRegisterMail(String email, String loginId, String tempPassword, String employeeNumber);
    void sendTempPassword(String email, String tempPassword);
    void sendHtmlMail(String to, String subject, String content);
}
