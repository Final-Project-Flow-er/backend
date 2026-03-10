package com.chaing.api.controller.franchise;

import com.chaing.api.facade.franchise.FranchiseInventoryLogFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@Tag(name = "FranchiseInventoryLog API", description = "가맹점 재고 로그 관련 API")
@RequestMapping("/api/v1/franchise/log")
public class FranchiseInventoryLogController {

    private final FranchiseInventoryLogFacade franchiseInventoryLogFacade;

    @Operation(summary = "가맹점 물류 입출고 이력 조회", description = "가맹점 물류 입출고 이력을 확인합니다.")
    @GetMapping("/inventory/{franchiseId}")
    public ResponseEntity<ApiResponse<FranchiseInventoryLogListResponse>> findFranchiseInboundOutboundLogs(
            @AuthenticationPrincipal UserPrincipal principal,

            @RequestParam(required = false) String productName,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String transactionCode,

            @PageableDefault(size = 10) Pageable pageable) {
        FranchiseLogRequest request = new FranchiseLogRequest(productName, startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse
                .success(franchiseInventoryLogFacade.findFranchiseInboundOutboundLogs(principal.getBusinessUnitId(), request, pageable)));
    }

    @Operation(summary = "가맹점 판매 환불 이력 조회", description = "가맹점 판매 환불 이력을 확인합니다.")
    @GetMapping("/sales/{franchiseId}")
    public ResponseEntity<ApiResponse<FranchiseInventoryLogListResponse>> findFranchiseSalesRefundLogs(
            @AuthenticationPrincipal UserPrincipal principal,

            @RequestParam(required = false) String productName,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @RequestParam(required = false) String transactionCode,

            @PageableDefault(size = 10) Pageable pageable) {
        FranchiseLogRequest request = new FranchiseLogRequest(productName, startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse
                .success(franchiseInventoryLogFacade.findFranchiseSalesRefundLogs(principal.getBusinessUnitId(), request, pageable)));
    }

}