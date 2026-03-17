package com.chaing.api.controller.user;

import com.chaing.api.dto.user.request.LoginRequest;
import com.chaing.api.dto.user.request.ResetPasswordRequest;
import com.chaing.api.dto.user.response.LoginResponse;
import com.chaing.api.facade.user.AuthFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "인증 API")
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthFacade authFacade;

    @Operation(summary = "로그인", description = "아이디와 비밀번호를 입력받아 인증 후 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(authFacade.login(request)));
    }

    @Operation(summary = "비밀번호 재설정", description = "등록된 이메일로 임시 비밀번호 전송")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authFacade.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("임시 비밀번호가 메일로 발송되었습니다."));
    }

    @Operation(summary = "토큰 재발급", description = "만료된 Access Token을 Refresh Token을 사용하여 갱신")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(
            @RequestHeader("Authorization-Refresh") String refreshToken
    ) {
        return ResponseEntity.ok(ApiResponse.success(authFacade.reissue(refreshToken)));
    }

    @Operation(summary = "로그아웃", description = "토큰을 무효화 시켜 로그아웃 처리")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authFacade.logout(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
