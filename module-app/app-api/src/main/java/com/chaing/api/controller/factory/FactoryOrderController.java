package com.chaing.api.controller.factory;

import com.chaing.api.dto.factory.request.FactoryOrderRequest;
import com.chaing.api.dto.factory.response.FactoryOrderResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Factory Order API", description = "공장 발주 관련 API")
@RequestMapping("/api/v1/factory/orders")
public class FactoryOrderController {

    @Operation(summary = "발주 조회", description = "본사의 발주 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FactoryOrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(List.of(FactoryOrderResponse.builder().build())));
    }

    @Operation(summary = "특정 발주 조회", description = "발주 번호로 본사의 특정 발주 조회")
    @GetMapping("/{order-number}")
    public ResponseEntity<ApiResponse<FactoryOrderResponse>> getOrder(
            @PathVariable("order-number") String orderNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(FactoryOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 상태 변경", description = "발주 상태를 접수, 반려 등으로 변경")
    @PatchMapping("/{order-number}")
    public ResponseEntity<ApiResponse<FactoryOrderResponse>> updateOrder(
            @PathVariable("order-number") String orderNumber,
            @RequestBody FactoryOrderRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FactoryOrderResponse.builder().build()));
    }
}
