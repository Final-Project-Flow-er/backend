package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementLogRequest;
import com.chaing.api.dto.hq.settlement.response.HQSettlementLogResponse; // 새로 만들 DTO (테이블 한 줄)
import com.chaing.api.facade.hq.HQSettlementLogFacade; // 새로 만들 Facade
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "HQSettlementLog API", description = "본사 정산 로그(이력) 조회 API")
@RequestMapping("/api/v1/hq/settlements/logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ', 'ADMIN')")
public class HQSettlementLogController {

    private final HQSettlementLogFacade logFacade;

    @Operation(summary = "정산 이력 목록 조회", description = """
            정산 이력 조회 화면의 테이블 데이터를 조회합니다.
            - type 탭 필터(ALL/CONFIRM/DOC/ADJUSTMENT/CANCEL)
            """)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<HQSettlementLogResponse>>> getSettlementLogs(
            @Valid HQSettlementLogRequest request) {
        Page<HQSettlementLogResponse> response = logFacade.getSettlementLogs(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
