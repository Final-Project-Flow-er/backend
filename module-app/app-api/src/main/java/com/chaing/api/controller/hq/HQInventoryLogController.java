package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.inventorylogs.request.HQFranchiseLogRequest;
import com.chaing.api.dto.hq.inventorylogs.request.HQLogRequest;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "HqInventoryLog API", description = "본사 재고 로그 관련 API")
@RequestMapping("/api/v1/hq/log")
public class HQInventoryLogController {

    @Operation(summary = "본사 반품 입고 이력 조회", description = "본사의 반품 입고 이력을 확인합니다.")
    @PostMapping("/return-inbound")
    public ResponseEntity<ApiResponse<?>> findReturnInboundLogs(
            @RequestBody HQLogRequest hqLogRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "본사 반품 출고 이력 조회", description = "본사의 반품 출고 이력을 확인합니다.")
    @PostMapping("/return-outbound/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findReturnOutboundLogs(
            @PathVariable Long franchiseId,
            @RequestBody HQLogRequest hqLogRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "본사 폐기 이력 조회", description = "본사의 폐기 이력을 확인합니다.")
    @PostMapping("/disposal")
    public ResponseEntity<ApiResponse<?>> findDisposalLogs(
            @RequestBody HQLogRequest hqLogRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가맹점 물류 입출고 이력 조회", description = "본사에서 가맹점 물류 입출고 이력을 확인합니다.")
    @PostMapping("/franchise-inventory/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findFranchiseInboundOutboundLogs(
            @PathVariable Long franchiseId,
            @RequestBody HQFranchiseLogRequest hqFranchiseLogRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가맹점 판매 환불 이력 조회", description = "본사에서 가맹점 판매 환불 이력을 확인합니다.")
    @PostMapping("/franchise-sales/{franchiseId}")
    public ResponseEntity<ApiResponse<?>> findFranchiseSalesRefundLogs(
            @PathVariable Long franchiseId,
            @RequestBody HQFranchiseLogRequest hqFranchiseLogRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "공장 재고 이력 조회", description = "본사에서 공장의 재고 이력을 확인합니다.")
    @PostMapping("/factory/{factoryId}")
    public ResponseEntity<ApiResponse<?>> findFactoryInventoryLogs(
            @PathVariable Long factoryId,
            @RequestBody HQLogRequest hqLogRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}