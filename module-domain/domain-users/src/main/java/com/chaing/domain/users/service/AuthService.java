package com.chaing.domain.users.service;

import com.chaing.domain.users.entity.User;

public interface AuthService {

    String hashToken(String token);
    void saveRefreshToken(Long userId, String token, Long expiration);
    String getRefreshToken(Long userId);
    void resetPassword(User user, String rawPassword);
    String generateTempPassword();
    void deleteRefreshToken(Long userId);
}
