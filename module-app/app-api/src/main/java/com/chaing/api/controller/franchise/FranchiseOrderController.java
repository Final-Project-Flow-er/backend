package com.chaing.api.controller.franchise;

import com.chaing.domain.orders.dto.request.FranchiseOrderCreateRequest;
import com.chaing.api.dto.franchise.orders.response.FranchiseOrderResponse;
import com.chaing.domain.orders.dto.request.FranchiseOrderUpdateRequest;
import com.chaing.api.facade.franchise.FranchiseOrderFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderCancelResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderCreateResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderDetailResponse;
import com.chaing.domain.orders.dto.response.FranchiseOrderUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'FRANCHISE', 'HQ')")
    public ResponseEntity<ApiResponse<List<FranchiseOrderResponse>>> getAllOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseOrderFacade.getAllOrders(userId)));
    }

    @Operation(summary = "특정 발주 조회", description = "가맹점 id와 발주 번호로 특정 발주 조회")
    @GetMapping("/{order-code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRANCHISE', 'HQ')")
    public ResponseEntity<ApiResponse<FranchiseOrderDetailResponse>> getOrder(
            @PathVariable("order-code") String orderCode,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseOrderFacade.getOrder(userId, orderCode)));
    }

    @Operation(summary = "발주 수정", description = "가맹점 id와 발주 번호로 특정 발주 수정")
    @PatchMapping("/{order-code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRANCHISE', 'HQ')")
    public ResponseEntity<ApiResponse<FranchiseOrderUpdateResponse>> updateOrder(
            @PathVariable("order-code") String orderCode,
            @RequestBody List<FranchiseOrderUpdateRequest> requests,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseOrderFacade.updateOrder(userId, orderCode, requests)));
    }

    @Operation(summary = "발주 취소", description = "가맹점 id와 발주 번호로 특정 발주 취소")
    @PatchMapping("/{order-code}/cancellation")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRANCHISE', 'HQ')")
    public ResponseEntity<ApiResponse<FranchiseOrderCancelResponse>> cancelOrder(
            @PathVariable("order-code") String orderCode,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseOrderFacade.cancelOrder(userId, orderCode)));
    }

    @Operation(summary = "발주 생성", description = "가맹점 id로 발주 생성")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FRANCHISE', 'HQ')")
    public ResponseEntity<ApiResponse<FranchiseOrderCreateResponse>> createOrder(
            @RequestBody FranchiseOrderCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseOrderFacade.createOrder(userId, request)));
    }
}
