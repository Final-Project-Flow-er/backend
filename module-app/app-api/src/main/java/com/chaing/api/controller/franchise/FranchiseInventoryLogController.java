package com.chaing.api.controller.franchise;

import com.chaing.api.dto.franchise.inventorylogs.request.FranchiseLogRequest;
import com.chaing.core.dto.ApiResponse;
import com.chaing.core.enums.LogType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@Tag(name = "FranchiseInventoryLog API", description = "가맹점 재고 로그 관련 API")
@RequestMapping("/api/v1/franchise/log")
public class FranchiseInventoryLogController {

    @Operation(summary = "가맹점 물류 입출고 이력 조회", description = "가맹점 물류 입출고 이력을 확인합니다.")
    @GetMapping("/inventory/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findFranchiseInboundOutboundLogs(
            @PathVariable Long franchiseId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) LogType logType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        FranchiseLogRequest request = new FranchiseLogRequest(productName, logType, startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가맹점 판매 환불 이력 조회", description = "가맹점 판매 환불 이력을 확인합니다.")
    @GetMapping("/sales/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findFranchiseSalesRefundLogs(
            @PathVariable Long franchiseId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) LogType logType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        FranchiseLogRequest request = new FranchiseLogRequest(productName, logType, startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}