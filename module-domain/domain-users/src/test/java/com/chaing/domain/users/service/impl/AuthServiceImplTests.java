package com.chaing.domain.users.service.impl;

import com.chaing.domain.users.entity.RefreshToken;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTests {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("refresh token 저장")
    void saveRefreshToken() {

        // given
        Long userId = 1L;
        String token = "refresh-token";
        Long expiration = 3600L;
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        authService.saveRefreshToken(userId, token, expiration);

        // then
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("refresh token 조회 (토큰이 없을 경우 예외 발생)")
    void getRefreshToken_NotFound_ThrowsException() {

        // given
        Long userId = 1L;
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UserException.class, () -> authService.getRefreshToken(userId));
    }

    @Test
    @DisplayName("비밀번호 재설정")
    void resetPassword() {

        // given
        User user = User.builder().password("oldPassword").build();
        String rawPassword = "rawPassword";
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // when
        authService.resetPassword(user, rawPassword);

        // then
        assertEquals(encodedPassword, user.getPassword());
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("임시 비밀번호 생성")
    void generateTempPassword() {

        // when
        String password = authService.generateTempPassword();

        // then
        assertNotNull(password);
        assertTrue(password.length() >= 8);
        System.out.println("생성된 임시 비밀번호: " + password);
    }

    @Test
    @DisplayName("refresh token 삭제")
    void deleteRefreshToken() {

        // given
        Long userId = 1L;

        // when
        authService.deleteRefreshToken(userId);

        // then
        verify(refreshTokenRepository, times(1)).deleteById(userId);
    }
}