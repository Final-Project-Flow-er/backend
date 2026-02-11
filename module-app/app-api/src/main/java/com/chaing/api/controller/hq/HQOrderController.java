package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.request.HQOrderCreateRequest;
import com.chaing.api.dto.hq.request.HQOrderUpdateRequest;
import com.chaing.api.dto.hq.request.HQOrderUpdateStatusRequest;
import com.chaing.api.dto.hq.response.HQOrderResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "HQ Order API", description = "본사 발주 관련 API")
@RequestMapping("/api/v1/hq/orders")
public class HQOrderController {

    @Operation(summary = "발주 조회", description = "본사의 발주 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<HQOrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(List.of(HQOrderResponse.builder().build())));
    }

    @Operation(summary = "특정 발주 조회", description = "발주 번호로 특정 발주 조회")
    @GetMapping("/{order-number}")
    public ResponseEntity<ApiResponse<HQOrderResponse>> getOrder(
            @PathVariable("order-number") String orderNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 수정", description = "발주 번호로 특정 발주 수정")
    @PatchMapping("/{order-number}")
    public ResponseEntity<ApiResponse<HQOrderResponse>> updateOrder(
            @PathVariable("order-number") String orderNumber,
            @RequestBody HQOrderUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 상태 변경", description = "접수, 반려 등 특정 발주의 상태 변경")
    @PatchMapping("/{order-number}/status")
    public ResponseEntity<ApiResponse<HQOrderResponse>> updateOrderStatus(
            @PathVariable("order-number") String orderNumber,
            @RequestBody HQOrderUpdateStatusRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 취소", description = "발주 번호로 특정 발주 취소")
    @PatchMapping("/{order-number}/cancellation")
    public ResponseEntity<ApiResponse<HQOrderResponse>> cancelOrder(
            @PathVariable("order-number") String orderNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 생성", description = "발주 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<HQOrderResponse>> createOrder(
            @RequestBody HQOrderCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQOrderResponse.builder().build()));
    }
}
