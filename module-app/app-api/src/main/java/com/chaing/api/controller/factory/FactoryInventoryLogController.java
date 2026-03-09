package com.chaing.api.controller.factory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import com.chaing.api.facade.factory.FactoryInventoryLogFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.inventorylogs.dto.request.FactoryLogRequest;
import com.chaing.domain.inventorylogs.dto.response.FactoryInventoryLogListResponse;
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

@RequiredArgsConstructor
@RestController
@Tag(name = "FactoryInventoryLog API", description = "공장 재고 로그 관련 API")
@RequestMapping("/api/v1/factory/log")
public class FactoryInventoryLogController {

    private final FactoryInventoryLogFacade factoryInventoryLogFacade;

    @Operation(summary = "공장 재고 이력 조회", description = "공장의 재고 이력을 확인합니다.")
    @GetMapping("/{factoryId}")
    public ResponseEntity<ApiResponse<FactoryInventoryLogListResponse>> findFactoryInventoryLogs(
            @PathVariable Long factoryId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String logType,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String transactionCode,

            @PageableDefault(size = 10) Pageable pageable) {
        FactoryLogRequest request = new FactoryLogRequest(productName, logType, startDate, endDate, transactionCode);
        return ResponseEntity.ok(
                ApiResponse.success(factoryInventoryLogFacade.findFactoryInventoryLogs(factoryId, request, pageable)));
    }
}