package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.response.HQReturnProductResponse;
import com.chaing.api.dto.hq.response.HQReturnResponse;
import com.chaing.api.facade.hq.HQReturnFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.returns.dto.request.HQOrderStatusUpdateRequest;
import com.chaing.domain.returns.dto.request.HQReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.HQOrderStatusShippingPendingResponse;
import com.chaing.domain.returns.dto.response.HQReturnDetailResponse;
import com.chaing.domain.returns.dto.response.HQReturnUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "HQ Return API", description = "본사 반품 관련 API")
@RequestMapping("/api/v1/hq/returns")
@RequiredArgsConstructor
public class HQReturnController {

    private final HQReturnFacade hqReturnFacade;

    @Operation(summary = "반품 요청 조회", description = "반품 요청 전체 조회 (페이지네이션)")
    @GetMapping
    @PreAuthorize("hasAnyRole('HQ', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<HQReturnResponse>>> getAllReturns(
            @RequestParam boolean isAll,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(hqReturnFacade.getAllReturnsPaged(isAll, pageable)));
    }

    @Operation(summary = "특정 반품 요청 조회", description = "특정 반품 요청 조회")
    @GetMapping("/{return-code}")
    @PreAuthorize("hasAnyRole('HQ', 'ADMIN')")
    public ResponseEntity<ApiResponse<HQReturnDetailResponse>> getReturn(
            @PathVariable("return-code") String returnCode
    ) {
        return ResponseEntity.ok(ApiResponse.success(hqReturnFacade.getReturn(returnCode)));
    }

    @Operation(summary = "반품 요청 상태 접수", description = "가맹점의 반품 요청 상태 접수")
    @PatchMapping
    @PreAuthorize("hasAnyRole('HQ', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<HQReturnProductResponse>>> inspectProduct(
            @RequestBody List<@NotBlank String> returnCodes
    ) {
        return ResponseEntity.ok(ApiResponse.success(hqReturnFacade.acceptReturn(returnCodes)));
    }

    @Operation(summary = "반품 제품 검수", description = "반품 요청 들어온 상품 검수")
    @PatchMapping("/{return-code}")
    @PreAuthorize("hasAnyRole('HQ', 'ADMIN')")
    public ResponseEntity<ApiResponse<HQReturnUpdateResponse>> updateReturn(
            @PathVariable("return-code") String returnCode,
            @Valid @RequestBody HQReturnUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(hqReturnFacade.updateReturn(returnCode, request)));
    }

    @Operation(summary = "반품 상태 수정", description = "차량 배정 후 해당 반품 상태를 배송 대기로 수정")
    @PatchMapping("/shipping-pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HQ')")
    public ResponseEntity<ApiResponse<List<HQOrderStatusShippingPendingResponse>>> updateOrderShippingPending(
            @RequestBody List<@Valid HQOrderStatusUpdateRequest> request
    ) {
        return ResponseEntity.ok(ApiResponse.success(hqReturnFacade.updateShippingPending(request)));
    }
}
