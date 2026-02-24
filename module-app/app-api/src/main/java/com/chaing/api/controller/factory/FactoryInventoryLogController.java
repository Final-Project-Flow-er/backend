package com.chaing.api.controller.factory;

import com.chaing.api.dto.factory.inventorylogs.request.FactoryLogRequest;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Tag(name = "FactoryInventoryLog API", description = "공장 재고 로그 관련 API")
@RequestMapping("/api/v1/factory/log")

public class FactoryInventoryLogController {
    @Operation(summary = "공장 재고 이력 조회", description = "공장의 재고 이력을 확인합니다.")
    @PostMapping("/{factoryId}")
    public ResponseEntity<ApiResponse<?>> findFactoryInventoryLogs(
            @PathVariable Long factoryId,
            @RequestBody FactoryLogRequest factoryLogRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
