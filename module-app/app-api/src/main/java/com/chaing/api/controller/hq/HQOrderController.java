package com.chaing.api.controller.hq;

import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.domain.orders.dto.request.HQOrderCreateRequest;
import com.chaing.domain.orders.dto.request.HQOrderUpdateStatusRequest;
import com.chaing.api.facade.factory.HQOrderFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.orders.dto.request.HQOrderUpdateRequest;
import com.chaing.domain.orders.dto.response.HQOrderCancelResponse;
import com.chaing.domain.orders.dto.response.HQOrderCreateResponse;
import com.chaing.domain.orders.dto.response.HQOrderDetailResponse;
import com.chaing.domain.orders.dto.response.HQOrderResponse;
import com.chaing.domain.orders.dto.response.HQOrderStatusUpdateResponse;
import com.chaing.domain.orders.dto.response.HQOrderUpdateResponse;
import com.chaing.domain.orders.dto.response.HQRequestedOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "HQ Order API", description = "본사 발주 관련 API")
@RequestMapping("/api/v1/hq/orders")
@RequiredArgsConstructor
public class HQOrderController {

    private final HQOrderFacade hqOrderFacade;

    @Operation(summary = "발주 조회", description = "본사의 발주 전체 조회")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HQ')")
    public ResponseEntity<ApiResponse<List<HQOrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.success(hqOrderFacade.getAllOrders()));
    }

    @Operation(summary = "특정 발주 조회", description = "발주 번호로 특정 발주 조회")
    @GetMapping("/{order-code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HQ')")
    public ResponseEntity<ApiResponse<HQOrderDetailResponse>> getOrder(
            @PathVariable("order-code") String orderCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(hqOrderFacade.getOrderDetail(orderCode)));
    }

    @Operation(summary = "발주 수정", description = "발주 번호로 특정 발주 수정")
    @PatchMapping("/{order-code}")
    public ResponseEntity<ApiResponse<HQOrderUpdateResponse>> updateOrder(
            @PathVariable("order-code") String orderCode,
            @Valid @RequestBody HQOrderUpdateRequest request
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(hqOrderFacade.updateOrder(username, orderCode, request)));
    }

    @Operation(summary = "가맹점 발주 요청 조회", description = "가맹점이 생성한 발주 요청 전체 조회")
    @GetMapping("/requested")
    public ResponseEntity<ApiResponse<List<HQRequestedOrderResponse>>> getRequestedOrders(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(hqOrderFacade.getRequestedOrders(userId)));
    }

    @Operation(summary = "가맹점의 발주 상태 변경", description = "가맹점의 발주의 상태를 접수/반려로 변경")
    @PatchMapping()
    public ResponseEntity<ApiResponse<List<HQOrderStatusUpdateResponse>>> updateOrderStatus(
            @Valid @RequestBody HQOrderUpdateStatusRequest request
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(hqOrderFacade.updateStatus(username, request)));
    }

    @Operation(summary = "발주 취소", description = "발주 번호로 특정 발주 취소")
    @PatchMapping("/{order-code}/cancellation")
    public ResponseEntity<ApiResponse<HQOrderCancelResponse>> cancelOrder(
            @PathVariable("order-code") String orderCode
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(hqOrderFacade.cancel(username, orderCode)));
    }

    @Operation(summary = "발주 생성", description = "본사 직원의 요청에 따른 발주 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<HQOrderCreateResponse>> createOrder(
            @Valid @RequestBody HQOrderCreateRequest request
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(hqOrderFacade.create(username, request)));
    }
}
