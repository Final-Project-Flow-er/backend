package com.chaing.api.controller.hq;

import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.SettlementLogType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "HQSettlementLog API", description = "본사 정산 로그(이력) 조회 API")
@RequestMapping("/api/v1/hq/settlements/logs")
@RequiredArgsConstructor
public class HQSettlementLogController {

    @Operation(
            summary = "정산 이력 목록 조회",
            description = """
                    정산 이력 조회 화면의 테이블 데이터를 조회합니다.
                    - type 탭 필터(ALL/CONFIRM/DOC/ADJUSTMENT/CANCEL)
                    """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getSettlementLogs(
            @RequestParam(value = "type", defaultValue = "ALL") SettlementLogType type,
            @RequestParam(value = "franchiseId", required = false) Long franchiseId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate to,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort
    ) {
        // 응답 예시(프론트 테이블 컬럼 매핑):
        // id(번호), type(유형), franchiseName(가맹점), content(내역), actorName(처리자), createdAt(일시)
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
    
}
