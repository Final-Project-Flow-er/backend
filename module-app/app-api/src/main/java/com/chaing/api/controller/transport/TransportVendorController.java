package com.chaing.api.controller.transport;

import com.chaing.api.dto.transport.management.request.CreateVendorRequest;
import com.chaing.api.dto.transport.management.request.UpdateVendorRequest;
import com.chaing.api.dto.transport.management.request.UpdateVendorStatusRequest;
import com.chaing.api.dto.transport.management.response.CreateVendorResponse;
import com.chaing.api.dto.transport.management.response.VendorDetailResponse;
import com.chaing.api.dto.transport.management.response.VendorSummaryResponse;
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
@Tag(name = "Transport Vendor API", description = "운송 업체 관리 API")
@RequestMapping("/api/v1/transport/vendors")
public class TransportVendorController {

    @Operation(summary = "운송 업체 등록", description = "본사 관리자가 운송 업체 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateVendorResponse>> createVendor(
            @Valid @RequestBody CreateVendorRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(CreateVendorResponse.builder().build()));
    }

    @Operation(summary = "운송 업체 목록 조회", description = "전체 운송 업체 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorSummaryResponse>>> getVendors() {
        return ResponseEntity.ok(ApiResponse.success(List.of(VendorSummaryResponse.builder().build())));
    }

    @Operation(summary = "운송 업체 상세 정보 조회", description = "특정 운송 업체의 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorDetailResponse>> getVendorDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(VendorDetailResponse.builder().build()));
    }

    @Operation(summary = "운송 업체 정보 수정", description = "특정 운송 업체의 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorDetailResponse>> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVendorRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(VendorDetailResponse.builder().build()));
    }

    @Operation(summary = "운송 업체 상태 변경", description = "운송 업체의 상태를 활성화, 비활성화, 또는 삭제 상태로 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VendorDetailResponse>> updateVendorStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVendorStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(VendorDetailResponse.builder().build()));
    }
}
