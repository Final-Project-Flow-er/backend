package com.chaing.domain.users.service.impl;

import com.chaing.domain.users.entity.RefreshToken;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.repository.RefreshTokenRepository;
import com.chaing.domain.users.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // refresh token 저장 (로그인)
    @Override
    public void saveRefreshToken(Long userId, String token, Long expiration) {
        refreshTokenRepository.findById(userId)
                .ifPresentOrElse(
                        existingToken -> existingToken.updateToken(token, expiration),
                        () -> refreshTokenRepository.save(RefreshToken.create(userId, token, expiration))
                );
    }

    // refresh token 조회 (토큰 재발급)
    @Override
    public String getRefreshToken(Long userId) {
        return refreshTokenRepository.findById(userId)
                .map(RefreshToken::getToken)
                .orElseThrow(() -> new UserException(UserErrorCode.REFRESH_TOKEN_NOT_FOUND));
    }

    // 비밀번호 재설정
    @Override
    public void resetPassword(User user, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.changePassword(encodedPassword);
    }

    // 임시 비밀번호 생성
    // (최소 8자리 이상이며, 알파벳 대소문자, 특수문자, 숫자 등 세 종류 이상의 문자를 포함)
    @Override
    public String generateTempPassword() {

        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*()-_=+";
        String allChars = upperCase + lowerCase + digits + specialChars;

        SecureRandom random = new SecureRandom();

        StringBuilder passwordBuilder = new StringBuilder();
        passwordBuilder.append(upperCase.charAt(random.nextInt(upperCase.length())));
        passwordBuilder.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        passwordBuilder.append(digits.charAt(random.nextInt(digits.length())));
        passwordBuilder.append(specialChars.charAt(random.nextInt(specialChars.length())));

        for (int i = 0; i < 4; i++) {
            passwordBuilder.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        List<Character> pwdChars = passwordBuilder.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars);

        return pwdChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    // refresh token 삭제 (로그아웃)
    @Override
    public void deleteRefreshToken(Long userId) {
        if (userId == null) {
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }
        refreshTokenRepository.deleteById(userId);
    }
}
