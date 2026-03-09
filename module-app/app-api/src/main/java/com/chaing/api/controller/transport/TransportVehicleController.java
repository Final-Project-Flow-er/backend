package com.chaing.api.controller.transport;

import com.chaing.api.dto.transport.management.request.CreateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleStatusRequest;
import com.chaing.api.dto.transport.management.response.VehicleDetailResponse;
import com.chaing.api.dto.transport.management.response.VehicleSummaryResponse;
import com.chaing.api.facade.transport.VehicleManagementFacade;
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
@Tag(name = "Transport Vehicle API", description = "운송 차량 관리 API")
@RequestMapping("/api/v1/transport/vehicles")
public class TransportVehicleController {

    private final VehicleManagementFacade vehicleManagementFacade;

    @Operation(summary = "운송 차량 등록", description = "본사 관리자가 운송 차량 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(vehicleManagementFacade.createVehicle(request)));
    }

    @Operation(summary = "운송 차량 목록 조회", description = "전체 운송 차량 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VehicleSummaryResponse>>> getVehicles(
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(vehicleManagementFacade.getVehicleList(pageable)));
    }

    @Operation(summary = "운송 차량 상세 정보 조회", description = "특정 운송 차량의 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> getVehicleDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(vehicleManagementFacade.getVehicleDetail(id)));
    }

    @Operation(summary = "운송 차량 정보 수정", description = "특정 운송 차량의 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(vehicleManagementFacade.updateVehicle(id, request)));
    }

    @Operation(summary = "운송 차량 상태 변경", description = "운송 차량의 상태를 활성화 또는 비활성화 상태로 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(vehicleManagementFacade.updateVehicleStatus(id, request)));
    }

    @Operation(summary = "운송 차량 삭제", description = "운송 차량 삭제 (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @PathVariable Long id
    ) {
        vehicleManagementFacade.deleteVehicle(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
