package com.chaing.api.controller.transport;

import com.chaing.api.dto.transport.internal.request.UpdateDeliverStatusRequest;
import com.chaing.api.dto.transport.internal.request.VehicleAssignmentRequest;
import com.chaing.api.dto.transport.internal.response.AvailableVehicleResponse;
import com.chaing.api.dto.transport.internal.response.TransportCancelResponse;
import com.chaing.api.dto.transport.internal.response.UnassignedOrderResponse;
import com.chaing.api.dto.transport.internal.response.UnassignedReturnResponse;
import com.chaing.api.facade.transport.InternalTransportFacade;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Inner Transport API", description = "내부 운송 관련 API")
@RequestMapping("/api/v1/transport/internal")
@RequiredArgsConstructor
public class    InternalTransportController {

    private final InternalTransportFacade transportFacade;

    @Operation(summary = "발주 운송 가능 차량 조회", description = "발주 건에 배차 가능 차량 리스트 출력한다.")
    @GetMapping("/available-vehicles")
    public ResponseEntity<ApiResponse<List<AvailableVehicleResponse>>> getAvailableVehicles() {

        List<AvailableVehicleResponse> response = transportFacade.getAvailableVehicle();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "차량 미배정 발주 목록 조회", description = "차량이 배정되지 않은 발주 목록을 조회한다.")
    @GetMapping("/unassigned-orders")
    public ResponseEntity<ApiResponse<List<UnassignedOrderResponse>>> getUnassignedOrders() {

        List<UnassignedOrderResponse> response = transportFacade.getUnassignedOrders();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "발주 차량 배정", description = "차량 미배정 발주 건에 차량 배정한다.")
    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<Void>> assignVehicle(
            @Valid @RequestBody VehicleAssignmentRequest request) {
        // TODO: 배정 로직 작성
        transportFacade.assignVehicle(request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배차 해제", description = "배정된 차량 해제한다.")
    @DeleteMapping("/assignments/{transportId}")
    public ResponseEntity<ApiResponse<TransportCancelResponse>> cancelAssignment(
            @PathVariable Long transportId) {
        // TODO: 해제 로직 작성
        TransportCancelResponse response = transportFacade.cancelAssignment(transportId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "반품 운송 가능 차량 조회", description = "반품 건에 배차 가능 차량 리스트 출력한다.")
    @GetMapping("/returns/available-vehicles")
    public ResponseEntity<ApiResponse<List<AvailableVehicleResponse>>> getReturnAvailableVehicles() {
        return ResponseEntity.ok(ApiResponse.success(transportFacade.getVehicleForReturn()));
    }

    @Operation(summary = "차량 미배정 반품 목록 조회", description = "차량이 배정되지 않은 반품 목록을 조회한다.")
    @GetMapping("/unassigned-returns")
    public ResponseEntity<ApiResponse<List<UnassignedReturnResponse>>> getUnassignedReturns() {
        return ResponseEntity.ok(ApiResponse.success((transportFacade.getUnassignedReturns())));
    }

    @Operation(summary = "반품 차량 배정", description = "차량 미배정 반품 건에 차량을 배정한다.")
    @PostMapping("/returns/assignments")
    public ResponseEntity<ApiResponse<Void>> assignReturns(
            @Valid @RequestBody VehicleAssignmentRequest request
    ) {
        transportFacade.assignVehicleReturns(request);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배송 상태 변경", description = "차량의 배송 상태를 변경한다.")
    @PatchMapping("/updated-deliver-status")
    public ResponseEntity<ApiResponse<Void>> updateDeliverStatusRequest(
            @Valid @RequestBody UpdateDeliverStatusRequest request
    ) {
        transportFacade.updateDeliverStatus(request.orderCodes());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
