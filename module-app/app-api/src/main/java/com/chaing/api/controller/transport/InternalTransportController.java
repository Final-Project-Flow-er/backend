package com.chaing.api.controller.transport;

import com.chaing.api.dto.transport.internal.request.ArrivalApprovalRequest;
import com.chaing.api.dto.transport.internal.request.TransportForceUpdateRequest;
import com.chaing.api.dto.transport.internal.request.VehicleAssignmentRequest;
import com.chaing.api.dto.transport.internal.response.AvailableVehicleResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Inner Transport API", description = "내부 운송 관련 API")
@RequestMapping("/api/v1/transport/internal")
public class InternalTransportController {

    @Operation(summary = "운송 가능 차량 조회", description = "배차 가능 차량 리스트 출력")
    @GetMapping("/available-vehicles")
    public ResponseEntity<ApiResponse<AvailableVehicleResponse>> getAvailableVehicles() {
        // TODO: 서비스 로직 연결
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "차량 배정", description = "미배정 주문 차량 배정")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<?>> assignVehicle(
            @RequestBody VehicleAssignmentRequest req) {
        // TODO: 배정 로직 작성
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배차 해제", description = "배정된 차량 해제")
    @DeleteMapping("/assign/{transportId}")
    public ResponseEntity<ApiResponse<?>> cancelAssignment(
            @PathVariable Long transportId) {
        // TODO: 해제 로직 작성
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "입고 승인 상태 변경", description = "입고 승인 시 상태 변경")
    @PatchMapping("/{transportId}/arrival-approve")
    public ResponseEntity<ApiResponse<?>> approveArrival(
            @PathVariable Long transportId, @RequestBody ArrivalApprovalRequest req) {
        // TODO: 상태 변경 로직 (예: ARRIVED)
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "상태 강제 수정", description = "운송 및 차량 상태 강제 수정")
    @PutMapping("/{transportId}/force-update")
    public ResponseEntity<ApiResponse<?>> forceUpdateStatus(
            @PathVariable Long transportId, @RequestBody TransportForceUpdateRequest req) {
        // TODO: 강제 수정 로직
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
