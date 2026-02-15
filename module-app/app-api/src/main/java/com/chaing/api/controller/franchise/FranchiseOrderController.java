package com.chaing.api.controller.franchise;

import com.chaing.api.dto.franchise.orders.request.FranchiseOrderCreateRequest;
import com.chaing.api.dto.franchise.orders.response.FranchiseOrderResponse;
import com.chaing.api.dto.franchise.orders.request.FranchiseOrderUpdateRequest;
import com.chaing.api.facade.franchise.FranchiseOrderFacade;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@Tag(name = "Franchise Order API", description = "가맹점 발주 관련 API")
@RequestMapping("/api/v1/franchise/orders")
@RequiredArgsConstructor
public class FranchiseOrderController {

    private final FranchiseOrderFacade franchiseOrderFacade;

    @Operation(summary = "발주 조회", description = "가맹점 id로 해당 가맹점의 발주 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FranchiseOrderResponse>>> getAllOrders() {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(franchiseOrderFacade.getAllOrders(username)));
    }

    @Operation(summary = "특정 발주 조회", description = "가맹점 id와 발주 번호로 특정 발주 조회")
    @GetMapping("/{order-number}")
    public ResponseEntity<ApiResponse<FranchiseOrderResponse>> getOrder(
            @PathVariable("order-number") String orderNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 수정", description = "가맹점 id와 발주 번호로 특정 발주 수정")
    @PatchMapping("/{order-number}")
    public ResponseEntity<ApiResponse<FranchiseOrderResponse>> updateOrder(
            @PathVariable("order-number") String orderNumber,
            @RequestBody FranchiseOrderUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 취소", description = "가맹점 id와 발주 번호로 특정 발주 취소")
    @PatchMapping("/{order-number}/cancellation")
    public ResponseEntity<ApiResponse<FranchiseOrderResponse>> cancelOrder(
            @PathVariable("order-number") String orderNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseOrderResponse.builder().build()));
    }

    @Operation(summary = "발주 생성", description = "가맹점 id로 발주 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<FranchiseOrderResponse>> createOrder(
            @RequestBody FranchiseOrderCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseOrderResponse.builder().build()));
    }
}
