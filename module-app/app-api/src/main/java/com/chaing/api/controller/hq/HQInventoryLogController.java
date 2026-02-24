package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.inventorylogs.request.HQFranchiseLogRequest;
import com.chaing.api.dto.hq.inventorylogs.request.HQLogRequest;
import com.chaing.api.dto.hq.inventorylogs.request.HQFactoryLogRequest;
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
@Tag(name = "HqInventoryLog API", description = "본사 재고 로그 관련 API")
@RequestMapping("/api/v1/hq/log")
public class HQInventoryLogController {

    @Operation(summary = "본사 반품 입고 이력 조회", description = "본사의 반품 입고 이력을 확인합니다.")
    @GetMapping("/return-inbound")
    public ResponseEntity<ApiResponse<?>> findReturnInboundLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        HQLogRequest request = new HQLogRequest(startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "본사 반품 출고 이력 조회", description = "본사의 반품 출고 이력을 확인합니다.")
    @GetMapping("/return-outbound/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findReturnOutboundLogs(
            @PathVariable Long franchiseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        HQLogRequest request = new HQLogRequest(startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "본사 폐기 이력 조회", description = "본사의 폐기 이력을 확인합니다.")
    @GetMapping("/disposal")
    public ResponseEntity<ApiResponse<?>> findDisposalLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        HQLogRequest request = new HQLogRequest(startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가맹점 물류 입출고 이력 조회", description = "본사에서 가맹점 물류 입출고 이력을 확인합니다.")
    @GetMapping("/franchise-inventory/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findFranchiseInboundOutboundLogs(
            @PathVariable Long franchiseId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) LogType logType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        HQFranchiseLogRequest request = new HQFranchiseLogRequest(productName, logType, startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가맹점 판매 환불 이력 조회", description = "본사에서 가맹점 판매 환불 이력을 확인합니다.")
    @GetMapping("/franchise-sales/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findFranchiseSalesRefundLogs(
            @PathVariable Long franchiseId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) LogType logType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        HQFranchiseLogRequest request = new HQFranchiseLogRequest(productName, logType, startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공장 재고 이력 조회", description = "본사에서 공장의 재고 이력을 확인합니다.")
    @GetMapping("/factory/{factoryId}")
    public ResponseEntity<ApiResponse<?>> findFactoryInventoryLogs(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) LogType logType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String serialCode
    ) {
        HQFactoryLogRequest request = new HQFactoryLogRequest(productName, logType, startDate, endDate, serialCode);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}