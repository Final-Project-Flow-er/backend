package com.chaing.api.controller.transport;

import com.chaing.api.dto.transport.management.request.CreateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleRequest;
import com.chaing.api.dto.transport.management.request.UpdateVehicleStatusRequest;
import com.chaing.api.dto.transport.management.response.CreateVehicleResponse;
import com.chaing.api.dto.transport.management.response.VehicleDetailResponse;
import com.chaing.api.dto.transport.management.response.VehicleSummaryResponse;
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
@Tag(name = "Transport Vehicle API", description = "운송 차량 관리 API")
@RequestMapping("/api/v1/transport/vehicles")
public class TransportVehicleController {

    @Operation(summary = "운송 차량 등록", description = "본사 관리자가 운송 차량 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateVehicleResponse>> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(CreateVehicleResponse.builder().build()));
    }

    @Operation(summary = "운송 차량 목록 조회", description = "전체 운송 차량 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VehicleSummaryResponse>>> getVehicles() {
        return ResponseEntity.ok(ApiResponse.success(List.of(VehicleSummaryResponse.builder().build())));
    }

    @Operation(summary = "운송 차량 상세 정보 조회", description = "특정 운송 차량의 상세 정보 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> getVehicleDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(VehicleDetailResponse.builder().build()));
    }

    @Operation(summary = "운송 차량 정보 수정", description = "특정 운송 차량의 정보 수정")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(VehicleDetailResponse.builder().build()));
    }

    @Operation(summary = "운송 차량 상태 변경", description = "운송 차량의 상태를 활성화, 비활성화, 또는 삭제 상태로 변경")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<VehicleDetailResponse>> updateVehicleStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(VehicleDetailResponse.builder().build()));
    }
}
