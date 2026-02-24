package com.chaing.api.controller.user;

import com.chaing.api.dto.user.request.ChangePasswordRequest;
import com.chaing.api.dto.user.request.UpdateMyInfoRequest;
import com.chaing.api.dto.user.request.UpdateMyWorkplaceInfoRequest;
import com.chaing.api.dto.user.response.MyInfoResponse;
import com.chaing.api.dto.user.response.MyWorkplaceInfoResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "MyPage API", description = "내 정보 관리 API")
@RequestMapping("/api/v1/users/me")
public class MyPageController {

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 상세 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<MyInfoResponse>> getMyInfo() {
        return ResponseEntity.ok(ApiResponse.success(MyInfoResponse.builder().build()));
    }

    @Operation(summary = "내 정보 수정", description = "로그인된 사용자의 정보 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<MyInfoResponse>> updateMyInfo(
            @Valid @RequestBody UpdateMyInfoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(MyInfoResponse.builder().build()));
    }

    @Operation(summary = "비밀번호 변경", description = "기존 비밀번호 확인 후 새로운 비밀번호로 변경")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 완료"));
    }

    @Operation(summary = "내 사업장 정보 조회", description = "로그인된 사용자가 소속된 사업장 정보 조회")
    @GetMapping("/workplace")
    public ResponseEntity<ApiResponse<MyWorkplaceInfoResponse>> getMyWorkplaceInfo() {
        return ResponseEntity.ok(ApiResponse.success(MyWorkplaceInfoResponse.builder().build()));
    }

    @Operation(summary = "내 사업장 정보 수정", description = "로그인된 사용자가 소속된 사업장 정보 수정")
    @PatchMapping("/workplace")
    public ResponseEntity<ApiResponse<MyWorkplaceInfoResponse>> updateMyWorkplaceInfo(
            @Valid @RequestBody UpdateMyWorkplaceInfoRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(MyWorkplaceInfoResponse.builder().build()));
    }
}
