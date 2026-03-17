package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementAdjustmentListRequest;
import com.chaing.api.dto.hq.settlement.request.HQSettlementAdjustmentVoucherRequest;
import com.chaing.api.dto.hq.settlement.response.HQAdjustmentFranchiseResponse;
import com.chaing.api.dto.hq.settlement.response.HQAdjustmentResponse;
import com.chaing.api.facade.hq.HQSettlementAdjustmentFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.VoucherType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@Tag(name = "HQVoucherAdjustment API", description = "본사 조정 전표 관리 API")
@RequestMapping("/api/v1/hq/settlements/voucher-adjustments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ', 'ADMIN')")
public class HQSettlementAdjustmentController {

    private final HQSettlementAdjustmentFacade adjustmentFacade;

    @Operation(summary = "가맹점 목록 조회(드롭다운)", description = "조정 전표 등록 시 가맹점 선택용 목록")
    @GetMapping("/franchises")
    public ResponseEntity<ApiResponse<List<HQAdjustmentFranchiseResponse>>> getFranchises() {
        List<HQAdjustmentFranchiseResponse> response = adjustmentFacade.getFranchisesForDropdown();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "조정 전표 유형 조회(드롭다운)", description = "조정 전표 등록 시 전표 유형 선택용")
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<VoucherType>>> getAdjustmentTypes() {
        List<VoucherType> response = adjustmentFacade.getAdjustmentTypes();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 등록
    @Operation(summary = "조정 전표 등록", description = """
            조정 전표 등록
            - franchiseId: 대상 가맹점
            - type: 조정 유형(예: 기타조정/손실보정 등)
            - occurredAt: 발생일
            - amount: 금액
            - isMinus: 차감 처리(마이너스) 체크 시 true
            - reason: 내역(조정 사유)
            """)
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createAdjustment(
            @Valid @RequestBody HQSettlementAdjustmentVoucherRequest request,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.chaing.api.security.principal.UserPrincipal principal) {
        adjustmentFacade.createAdjustment(request, principal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "조정 전표 목록 조회", description = """
            등록된 조정 전표 목록 조회
            - month(yyyy-MM) 기준
            - franchiseId/type 필터(옵션)
            """)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<HQAdjustmentResponse>>> getAdjustments(
            @Valid HQSettlementAdjustmentListRequest request) {
        Page<HQAdjustmentResponse> response = adjustmentFacade.getAdjustments(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
