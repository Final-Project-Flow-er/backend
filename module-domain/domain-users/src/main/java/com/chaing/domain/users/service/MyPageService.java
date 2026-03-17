package com.chaing.domain.users.service;

import com.chaing.domain.users.dto.command.MyInfoUpdateCommand;
import com.chaing.domain.users.dto.command.PasswordUpdateCommand;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 조회
    public User getMyInfo(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    // 내 정보 수정
    public User updateMyProfile(Long userId, MyInfoUpdateCommand command) {
        User user = getMyInfo(userId);
        user.updateMyProfile(command);
        return user;
    }

    // 비밀번호 변경
    public User updatePassword(Long userId, PasswordUpdateCommand command) {
        User user = getMyInfo(userId);

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(command.newPassword());
        user.updatePassword(encodedPassword);
        return user;
    }
}
