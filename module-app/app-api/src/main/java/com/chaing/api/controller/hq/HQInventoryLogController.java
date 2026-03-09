package com.chaing.api.controller.hq;

import com.chaing.api.facade.hq.HQInventoryLogFacade;
import com.chaing.api.facade.inventorylogs.InventoryLogFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.request.FranchiseLogRequest;
import com.chaing.domain.inventorylogs.dto.request.LogRequest;
import com.chaing.domain.inventorylogs.dto.response.BoxCodeResponse;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.FranchiseInventoryLogListResponse;
import com.chaing.domain.inventorylogs.dto.response.InventoryLogListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name = "HqInventoryLog API", description = "본사 재고 로그 관련 API")
@RequestMapping("/api/v1/hq/log")
public class HQInventoryLogController {

    private final HQInventoryLogFacade hqInventoryLogFacade;
    private final InventoryLogFacade inventoryLogFacade;

    @Operation(summary = "본사 반품 입고 이력 조회", description = "본사의 반품 입고 이력을 확인합니다.")
    @GetMapping("/return-inbound")
    public ResponseEntity<ApiResponse<InventoryLogListResponse>> findReturnInboundLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            String transactionCode
    ) {
        LogRequest request = new LogRequest(startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryLogFacade.findReturnInboundLogs(request)));
    }

    @Operation(summary = "본사 반품 출고 이력 조회", description = "본사의 반품 출고 이력을 확인합니다.")
    @GetMapping("/return-outbound")
    public ResponseEntity<ApiResponse<InventoryLogListResponse>> findReturnOutboundLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) String transactionCode
    ) {
        LogRequest request = new LogRequest(startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryLogFacade.findReturnOutboundLogs(request)));
    }

    @Operation(summary = "본사 폐기 이력 조회", description = "본사의 폐기 이력을 확인합니다.")
    @GetMapping("/disposal")
    public ResponseEntity<ApiResponse<InventoryLogListResponse>> findDisposalLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) String transactionCode
    ) {
        LogRequest request = new LogRequest(startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryLogFacade.findDisposalLogs(request)));
    }

    @Operation(summary = "가맹점 물류 입출고 이력 조회", description = "본사에서 가맹점 물류 입출고 이력을 확인합니다.")
    @GetMapping("/franchise-inventory/{franchiseId}")
    public ResponseEntity<ApiResponse<FranchiseInventoryLogListResponse>> findFranchiseInboundOutboundLogs(
            @PathVariable Long franchiseId,
            @RequestParam(required = false)
            String productName,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false) String transactionCode
    ) {
        FranchiseLogRequest request = new FranchiseLogRequest(productName, startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryLogFacade.findFranchiseInboundOutboundLogs(franchiseId, request)));
    }

    @GetMapping("/logs/boxes")
    public ResponseEntity<ApiResponse<List<BoxCodeResponse>>> getBoxes(@RequestParam String transactionCode) {
        return ResponseEntity.ok(ApiResponse.success(inventoryLogFacade.getBoxCodes(transactionCode)));
    }

    @Operation(summary = "가맹점 판매 환불 이력 조회", description = "본사에서 가맹점 판매 환불 이력을 확인합니다.")
    @GetMapping("/franchise-sales/{franchiseId}")
    public ResponseEntity<ApiResponse<FranchiseInventoryLogListResponse>> findFranchiseSalesRefundLogs(
            @PathVariable Long franchiseId,
            @RequestParam(required = false)
            String productName,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            String transactionCode
    ) {
        FranchiseLogRequest request = new FranchiseLogRequest(productName, startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryLogFacade.findFranchiseSalesRefundLogs(franchiseId, request)));
    }

    @Operation(summary = "공장 재고 이력 조회", description = "본사에서 공장의 재고 이력을 확인합니다.")
    @GetMapping("/factory/{factoryId}")
    public ResponseEntity<ApiResponse<FactoryInventoryLogListResponse>> findFactoryInventoryLogs(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String logType,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(required = false) String transactionCode
    ) {
        FactoryLogRequest request = new FactoryLogRequest(productName, logType, startDate, endDate, transactionCode);
        return ResponseEntity.ok(ApiResponse.success(hqInventoryLogFacade.findFactoryInventoryLogs(factoryId, request)));
    }
}