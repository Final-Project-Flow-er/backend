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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    // 토큰 해싱
    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new UserException(UserErrorCode.HASH_ALGORITHM_NOT_FOUND);
        }
    }

    // refresh token 저장 (로그인)
    @Override
    public void saveRefreshToken(Long userId, String token, Long expiration) {
        String hashedToken = hashToken(token);

        RefreshToken refreshToken = refreshTokenRepository.findById(userId)
                .map(existingToken -> {
                    existingToken.updateToken(hashedToken, expiration);
                    return existingToken;
                })
                .orElseGet(() -> RefreshToken.create(userId, hashedToken, expiration));

        refreshTokenRepository.save(refreshToken);
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

        for (int i = 0; i < 6; i++) {
            passwordBuilder.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        List<Character> pwdChars = passwordBuilder.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(pwdChars, random);

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
