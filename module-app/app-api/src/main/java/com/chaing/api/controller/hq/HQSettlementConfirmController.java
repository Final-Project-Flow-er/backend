package com.chaing.api.controller.hq;

import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@Tag(name = "HQSettlementConfirm API", description = "본사 정산 확정(작성중 -> 확정요청 -> 최종확정)")
@RequestMapping("/api/v1/hq/settlement-confirm")
@RequiredArgsConstructor
public class HQSettlementConfirmController {

    //상단 카드: 작성중/확정요청/최종확정 개수
    @Operation(
            summary = "정산 확정 현황 카드 조회",
            description = """
                    상단 카드(작성중/확정요청/최종확정) 카운트 조회
                    - month(yyyy-MM) 기준
                    """
    )
    @GetMapping("/monthly/status-counts")
    public ResponseEntity<ApiResponse<?>> getMonthlyStatusCounts(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    //테이블: 월별 정산 확정 목록
    @Operation(
            summary = "월별 정산 확정 목록 조회",
            description = """
                    테이블 목록 조회(가맹점, 최종 정산 금액, 상태, 작업)
                    - month(yyyy-MM) 기준
                    - status 필터(옵션): 작성중/확정요청/최종확정
                    - keyword(옵션): 가맹점 검색
                    """
    )
    @GetMapping("/monthly/franchises")
    public ResponseEntity<ApiResponse<?>> getMonthlyConfirmList(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "status", required = false) SettlementStatus status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    //작성중 -> 확정요청
    @Operation(
            summary = "확정 요청",
            description = """
                    특정 가맹점 월별 정산을 '확정요청' 상태로 변경
                    (작성중 -> 확정요청)
                    """
    )
    @PostMapping("/monthly/franchises/{franchiseId}/request")
    public ResponseEntity<ApiResponse<?>> requestConfirm(
            @PathVariable Long franchiseId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    //확정요청 -> 최종확정
    @Operation(
            summary = "최종 확정(단건)",
            description = """
                    특정 가맹점 월별 정산을 '최종확정' 처리
                    (확정요청 -> 최종확정)
                    """
    )
    @PostMapping("/monthly/franchises/{franchiseId}/finalize")
    public ResponseEntity<ApiResponse<?>> finalizeConfirm(
            @PathVariable Long franchiseId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    //확정요청 -> 작성중 (수정 버튼)
    @Operation(
            summary = "확정요청 수정(되돌리기)",
            description = """
                    확정요청 상태를 작성중으로 되돌림
                    (확정요청 -> 작성중)
                    """
    )
    @PostMapping("/monthly/franchises/{franchiseId}/rollback")
    public ResponseEntity<ApiResponse<?>> rollbackToDraft(
            @PathVariable Long franchiseId,
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    //전체 확정: 확정요청 상태인 가맹점 일괄 최종확정
    @Operation(
            summary = "전체 확정(일괄)",
            description = """
                    선택 월의 확정요청 상태 가맹점들을 일괄 '최종확정' 처리
                    - 예: 20개 가맹점 일괄 확정
                    """
    )
    @PostMapping("/monthly/finalize-all")
    public ResponseEntity<ApiResponse<?>> finalizeAll(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
