package com.chaing.api.controller.franchise;

import com.chaing.api.dto.franchise.management.request.CreateFranchiseRequest;
import com.chaing.api.dto.franchise.management.request.CreateWarningRequest;
import com.chaing.api.dto.franchise.management.request.UpdateFranchiseRequest;
import com.chaing.api.dto.franchise.management.request.UpdateFranchiseStatusRequest;
import com.chaing.api.dto.franchise.management.response.CreateFranchiseResponse;
import com.chaing.api.dto.franchise.management.response.FranchiseInfoResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Franchise Management API", description = "가맹점 관리 API")
@RequestMapping("/api/v1/franchises")
public class FranchiseManagementController {

    @Operation(summary = "신규 가맹점 등록", description = "본사 관리자가 새로운 가맹점 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateFranchiseResponse>> createFranchise(
            @Valid @RequestBody CreateFranchiseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(CreateFranchiseResponse.builder().build()));
    }

    @Operation(summary = "가맹점 목록 조회", description = "전체 가맹점 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FranchiseInfoResponse>>> getFranchises() {
        return ResponseEntity.ok(ApiResponse.success(List.of(FranchiseInfoResponse.builder().build())));
    }

    @Operation(summary = "가맹점 상세 정보 조회", description = "특정 가맹점의 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FranchiseInfoResponse>> getFranchiseDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseInfoResponse.builder().build()));
    }

    @Operation(summary = "가맹점 정보 수정", description = "특정 가맹점의 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FranchiseInfoResponse>> updateFranchise(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFranchiseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseInfoResponse.builder().build()));
    }

    @Operation(summary = "가맹점 상태 변경", description = "가맹점의 상태를 활성화, 비활성화, 또는 삭제 상태로 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<FranchiseInfoResponse>> updateFranchiseStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFranchiseStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseInfoResponse.builder().build()));
    }

    @Operation(summary = "가맹점 경고 부여", description = "경고 1회 부여. 누적 3회 시 해당 가맹점은 자동으로 패널티 상태로 변경")
    @PostMapping("/{id}/warnings")
    public ResponseEntity<ApiResponse<FranchiseInfoResponse>> addWarning(
            @PathVariable Long id,
            @Valid @RequestBody CreateWarningRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseInfoResponse.builder().build()));
    }
}
