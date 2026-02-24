package com.chaing.api.controller.factory;

import com.chaing.api.dto.factory.management.request.CreateFactoryRequest;
import com.chaing.api.dto.factory.management.request.UpdateFactoryRequest;
import com.chaing.api.dto.factory.management.request.UpdateFactoryStatusRequest;
import com.chaing.api.dto.factory.management.response.CreateFactoryResponse;
import com.chaing.api.dto.factory.management.response.FactoryInfoResponse;
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
@Tag(name = "Factory Management API", description = "공장 관리 API")
@RequestMapping("/api/v1/factories")
public class FactoryManagementController {

    @Operation(summary = "신규 공장 등록", description = "본사 관리자가 새로운 공장 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateFactoryResponse>> createFactory(
            @Valid @RequestBody CreateFactoryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(CreateFactoryResponse.builder().build()));
    }

    @Operation(summary = "공장 목록 조회", description = "전체 공장 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FactoryInfoResponse>>> getFactories() {
        return ResponseEntity.ok(ApiResponse.success(List.of(FactoryInfoResponse.builder().build())));
    }

    @Operation(summary = "공장 상세 정보 조회", description = "특정 공장의 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FactoryInfoResponse>> getFactoryDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(FactoryInfoResponse.builder().build()));
    }

    @Operation(summary = "공장 정보 수정", description = "특정 공장의 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<FactoryInfoResponse>> updateFactory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFactoryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FactoryInfoResponse.builder().build()));
    }

    @Operation(summary = "공장 상태 변경", description = "공장의 상태를 활성화, 비활성화, 또는 삭제 상태로 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<FactoryInfoResponse>> updateFactoryStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFactoryStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FactoryInfoResponse.builder().build()));
    }
}
