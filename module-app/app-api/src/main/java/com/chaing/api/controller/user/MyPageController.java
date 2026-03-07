package com.chaing.api.controller.user;

import com.chaing.api.dto.hq.businessunit.response.BusinessUnitDetailResponse;
import com.chaing.api.dto.user.request.ChangePasswordRequest;
import com.chaing.api.dto.user.request.UpdateMyBusinessUnitInfoRequest;
import com.chaing.api.dto.user.request.UpdateMyInfoRequest;
import com.chaing.api.dto.user.response.MyInfoResponse;
import com.chaing.api.facade.user.MyPageFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "MyPage API", description = "내 정보 관리 API")
@RequestMapping("/api/v1/users/me")
public class MyPageController {

    private final MyPageFacade myPageFacade;

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 상세 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<MyInfoResponse>> getMyInfo(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MyInfoResponse response = myPageFacade.getMyInfo(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 정보 수정", description = "로그인된 사용자의 정보 수정")
    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MyInfoResponse>> updateMyInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") @Valid UpdateMyInfoRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        MyInfoResponse response = myPageFacade.updateMyProfile(principal.getId(), request, profileImage);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "비밀번호 변경", description = "기존 비밀번호 확인 후 새로운 비밀번호로 변경")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        myPageFacade.updatePassword(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 완료"));
    }

    @Operation(summary = "내 사업장 정보 조회", description = "로그인된 사용자가 소속된 사업장 정보 조회")
    @GetMapping("/workplace")
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> getMyWorkplaceInfo(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        BusinessUnitDetailResponse response = myPageFacade.getMyBusinessUnitInfo(principal.getRole(), principal.getBusinessUnitId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 사업장 정보 수정", description = "로그인된 사용자가 소속된 사업장 정보 수정")
    @PatchMapping("/workplace")
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> updateMyWorkplaceInfo(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateMyBusinessUnitInfoRequest request
    ) {
        BusinessUnitDetailResponse response = myPageFacade.updateMyBusinessUnitInfo(principal.getRole(), principal.getBusinessUnitId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
