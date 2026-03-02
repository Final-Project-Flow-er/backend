package com.chaing.api.controller.hq;


import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.VoucherType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@Tag(name = "HQVoucherAdjustment API", description = "본사 조정 전표 관리 API")
@RequestMapping("/api/v1/hq/voucher-adjustments")
@RequiredArgsConstructor
public class HQSettlementAdjustmentController {

    @Operation(summary = "가맹점 목록 조회(드롭다운)", description = "조정 전표 등록 시 가맹점 선택용 목록")
    @GetMapping("/franchises")
    public ResponseEntity<ApiResponse<?>> getFranchises() {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @Operation(summary = "조정 전표 유형 조회(드롭다운)", description = "조정 전표 등록 시 전표 유형 선택용")
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<?>> getAdjustmentTypes() {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    //등록
    @Operation(
            summary = "조정 전표 등록",
            description = """
                    조정 전표 등록
                    - franchiseId: 대상 가맹점
                    - type: 조정 유형(예: 기타조정/손실보정 등)
                    - occurredAt: 발생일
                    - amount: 금액
                    - isMinus: 차감 처리(마이너스) 체크 시 true
                    - reason: 내역(조정 사유)
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAdjustment(
            @RequestParam Long franchiseId,
            @RequestParam VoucherType type,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate occurredAt,
            @RequestParam Long amount,
            @RequestParam(defaultValue = "false") boolean isMinus,
            @RequestParam String reason
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
            summary = "조정 전표 목록 조회",
            description = """
                    등록된 조정 전표 목록 조회
                    - month(yyyy-MM) 기준
                    - franchiseId/type 필터(옵션)
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAdjustments(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "franchiseId", required = false) Long franchiseId,
            @RequestParam(value = "type", required = false) VoucherType type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
}
