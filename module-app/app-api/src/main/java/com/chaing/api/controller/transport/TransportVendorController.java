package com.chaing.api.controller.transport;

import com.chaing.api.dto.transport.management.request.CreateTransportRequest;
import com.chaing.api.dto.transport.management.request.TransportSearchRequest;
import com.chaing.api.dto.transport.management.request.UpdateTransportRequest;
import com.chaing.api.dto.transport.management.request.UpdateTransportStatusRequest;
import com.chaing.api.dto.transport.management.response.TransportDetailResponse;
import com.chaing.api.dto.transport.management.response.TransportSummaryResponse;
import com.chaing.api.facade.transport.TransportManagementFacade;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "Transport Vendor API", description = "운송 업체 관리 API")
@RequestMapping("/api/v1/transport/vendors")
public class TransportVendorController {

    private final TransportManagementFacade transportManagementFacade;

    @Operation(summary = "운송 업체 등록", description = "본사 관리자가 운송 업체 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<TransportDetailResponse>> createVendor(
            @Valid @RequestBody CreateTransportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(transportManagementFacade.createTransport(request)));
    }

    @Operation(summary = "운송 업체 목록 조회", description = "전체 운송 업체 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransportSummaryResponse>>> getVendors(
            TransportSearchRequest request,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(transportManagementFacade.getTransportList(request, pageable)));
    }

    @Operation(summary = "운송 업체 상세 정보 조회", description = "특정 운송 업체의 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportDetailResponse>> getVendorDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(transportManagementFacade.getTransportDetail(id)));
    }

    @Operation(summary = "운송 업체 정보 수정", description = "특정 운송 업체의 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TransportDetailResponse>> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransportRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(transportManagementFacade.updateTransport(id, request)));
    }

    @Operation(summary = "운송 업체 상태 변경", description = "운송 업체의 상태를 활성화 또는 비활성화 상태로 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TransportDetailResponse>> updateVendorStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransportStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(transportManagementFacade.updateTransportStatus(id, request)));
    }

    @Operation(summary = "운송 업체 삭제", description = "운송 업체 삭제 (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(
            @PathVariable Long id
    ) {
        transportManagementFacade.deleteTransport(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
