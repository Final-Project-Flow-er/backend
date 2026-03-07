package com.chaing.api.facade.user;

import com.chaing.api.dto.user.event.PasswordResetEvent;
import com.chaing.api.dto.user.request.LoginRequest;
import com.chaing.api.dto.user.request.ResetPasswordRequest;
import com.chaing.api.dto.user.response.LoginResponse;
import com.chaing.api.security.jwt.JwtProvider;
import com.chaing.api.security.principal.CustomUserDetailsService;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.domain.users.entity.User;
import com.chaing.domain.users.enums.UserAction;
import com.chaing.domain.users.enums.UserRole;
import com.chaing.domain.users.exception.UserErrorCode;
import com.chaing.domain.users.exception.UserException;
import com.chaing.domain.users.service.AuthService;
import com.chaing.domain.users.service.UserLogService;
import com.chaing.domain.users.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthFacade {

    private final CustomUserDetailsService userDetailsService;
    private final AuthService authService;
    private final UserManagementService userManagementService;
    private final UserLogService userLogService;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    // 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {

        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(request.loginId());

        if (!passwordEncoder.matches(request.password(), principal.getPassword())) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }

        User user = userManagementService.getUserById(principal.getId());

        String accessToken = jwtProvider.createAccessToken(user);
        String refreshToken = jwtProvider.createRefreshToken(user);

        authService.saveRefreshToken(user.getUserId(), refreshToken, jwtProvider.getRefreshTokenExpireTime() / 1000);

        return new LoginResponse(accessToken, refreshToken, principal.getRole());
    }

    // 비밀번호 재설정 (이메일 전송)
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        User user = userManagementService.getUserByLoginId(request.loginId());
        if (!user.getEmail().equals(request.email())) {
            throw new UserException(UserErrorCode.EMAIL_MISMATCH);
        }

        String tempPassword = authService.generateTempPassword();

        authService.resetPassword(user, tempPassword);
        userLogService.saveLog(user, user.getUserId(), UserAction.PASSWORD_UPDATE);
        eventPublisher.publishEvent(new PasswordResetEvent(request.email(), tempPassword));
    }

    // 토큰 재발급
    @Transactional
    public LoginResponse reissue(String refreshToken, UserRole userRole) {

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new UserException(UserErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);
        String savedToken = authService.getRefreshToken(userId);

        if (!savedToken.equals(authService.hashToken(refreshToken))) {
            throw new UserException(UserErrorCode.TOKEN_MISMATCH);
        }

        User user = userManagementService.getUserById(userId);

        String newAccessToken = jwtProvider.createAccessToken(user);
        String newRefreshToken = jwtProvider.createRefreshToken(user);

        authService.saveRefreshToken(user.getUserId(), newRefreshToken, jwtProvider.getRefreshTokenExpireTime() / 1000);

        return new LoginResponse(newAccessToken, newRefreshToken, userRole);
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
        authService.deleteRefreshToken(userId);
    }
}
