package com.chaing.api.controller.user;

import com.chaing.api.dto.user.request.LoginRequest;
import com.chaing.api.dto.user.request.ResetPasswordRequest;
import com.chaing.api.dto.user.response.LoginResponse;
import com.chaing.api.dto.user.response.ResetPasswordResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Auth API", description = "인증 API")
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Operation(summary = "로그인", description = "아이디와 비밀번호를 입력받아 인증 후 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(LoginResponse.builder().build()));
    }

    @Operation(summary = "비밀번호 재설정", description = "등록된 이메일로 임시 비밀번호 전송")
    @PostMapping("/password-reset")
    public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(ResetPasswordResponse.builder().build()));
    }

    @Operation(summary = "로그아웃", description = "토큰을 무효화 시켜 로그아웃 처리")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("로그아웃 성공"));
    }
}
