package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.user.request.CreateUserRequest;
import com.chaing.api.dto.hq.user.request.UpdateUserRequest;
import com.chaing.api.dto.hq.user.request.UpdateUserStatusRequest;
import com.chaing.api.dto.hq.user.response.CreateUserResponse;
import com.chaing.api.dto.hq.user.response.UserDetailResponse;
import com.chaing.api.dto.hq.user.response.UserLogResponse;
import com.chaing.api.dto.hq.user.response.UserSummaryResponse;
import com.chaing.api.facade.hq.UserManagementFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('HQ')")
@Tag(name = "User Management API", description = "회원 관리 API")
@RequestMapping("/api/v1/hq/users")
public class UserManagementController {

    private final UserManagementFacade userManagementFacade;

    @Operation(summary = "신규 회원 등록", description = "본사 관리자가 새로운 회원 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateUserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(userManagementFacade.registerUser(request, principal.getId())));
    }

    @Operation(summary = "회원 목록 조회", description = "전체 회원 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserSummaryResponse>>> getUserList(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userManagementFacade.getUserList(pageable)));
    }

    @Operation(summary = "회원 상세 정보 조회", description = "특정 회원의 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(userManagementFacade.getUserById(id)));
    }

    @Operation(summary = "회원 정보 수정", description = "특정 회원의 정보 및 권한 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDetailResponse response = userManagementFacade.updateUser(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원 상태 변경", description = "회원의 상태를 활성화, 비활성화, 또는 탈퇴 상태로 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserDetailResponse>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserDetailResponse response = userManagementFacade.updateUserStatus(id, request.status(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원 로그 조회", description = "회원의 등록, 수정, 상태 변경 이력 조회")
    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<UserLogResponse>>> getUserLog(
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(userManagementFacade.getUserLogs(pageable)));
    }

    @Operation(summary = "회원 삭제", description = "본사 관리자가 등록되어 있는 회원 정보 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userManagementFacade.deleteUser(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
