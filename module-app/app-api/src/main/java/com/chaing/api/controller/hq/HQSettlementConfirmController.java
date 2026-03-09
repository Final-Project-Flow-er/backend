package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementConfirmMonthlyRequest;
import com.chaing.api.dto.hq.settlement.response.HQConfirmStatusCountResponse; // 새로 만들 DTO (상단 3개 카드 카운트)
import com.chaing.api.dto.hq.settlement.response.HQConfirmFranchiseResponse; // 새로 만들 DTO (테이블 한 줄)
import com.chaing.api.facade.hq.HQSettlementConfirmFacade; // 새로 만들 Facade
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.SettlementStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/hq/settlements/confirm")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ', 'ADMIN')")
public class HQSettlementConfirmController {

        private final HQSettlementConfirmFacade confirmFacade;

        // 상단 카드: 작성중/확정요청/최종확정 개수
        @Operation(summary = "정산 확정 현황 카드 조회", description = """
                        상단 카드(작성중/확정요청/최종확정) 카운트 조회
                        - month(yyyy-MM) 기준
                        """)
        @GetMapping("/monthly/status-counts")
        public ResponseEntity<ApiResponse<HQConfirmStatusCountResponse>> getMonthlyStatusCounts(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                // [수정] 더미 대신 상태별 카운트를 담은 DTO 반환
                HQConfirmStatusCountResponse response = confirmFacade.getMonthlyStatusCounts(month);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        // 테이블: 월별 정산 확정 목록
        @Operation(summary = "월별 정산 확정 목록 조회", description = """
                        테이블 목록 조회(가맹점, 최종 정산 금액, 상태, 작업)
                        - month(yyyy-MM) 기준
                        - status 필터(옵션): 작성중/확정요청/최종확정
                        - keyword(옵션): 가맹점 검색
                        """)
        @GetMapping("/monthly/franchises")
        public ResponseEntity<ApiResponse<Page<HQConfirmFranchiseResponse>>> getMonthlyConfirmList(
                        @Valid HQSettlementConfirmMonthlyRequest request // [UPDATED] 긴 파라미터들을 DTO로 통합
        ) {
                // [수정] Facade 호출
                Page<HQConfirmFranchiseResponse> response = confirmFacade.getMonthlyConfirmList(request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        // 작성중 -> 확정요청
        @Operation(summary = "확정 요청", description = """
                        특정 가맹점 월별 정산을 '확정요청' 상태로 변경
                        (작성중 -> 확정요청)
                        """)
        @PostMapping("/monthly/franchises/{franchiseId}/request")
        public ResponseEntity<ApiResponse<Void>> requestConfirm(
                        @PathVariable Long franchiseId,
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                confirmFacade.requestConfirm(franchiseId, month);
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        // 확정요청 -> 최종확정
        @Operation(summary = "최종 확정(단건)", description = """
                        특정 가맹점 월별 정산을 '최종확정' 처리
                        (확정요청 -> 최종확정)
                        """)
        @PostMapping("/monthly/franchises/{franchiseId}/finalize")
        public ResponseEntity<ApiResponse<Void>> finalizeConfirm(
                        @PathVariable Long franchiseId,
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                confirmFacade.finalizeConfirm(franchiseId, month);
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        // 최종확정/확정요청 -> 작성중 (수정 버튼, 되돌리기)
        @Operation(summary = "확정요청 수정(되돌리기)", description = """
                        확정 상태를 작성중으로 되돌림
                        (확정요청 -> 작성중)
                        """)
        @PostMapping("/monthly/franchises/{franchiseId}/rollback")
        public ResponseEntity<ApiResponse<Void>> rollbackToDraft(
                        @PathVariable Long franchiseId,
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                confirmFacade.rollbackToDraft(franchiseId, month);
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        // 전체 확정: 확정요청 상태인 가맹점 일괄 최종확정
        @Operation(summary = "전체 확정(일괄)", description = """
                        선택 월의 확정요청 상태 가맹점들을 일괄 '최종확정' 처리
                        - 예: 20개 가맹점 일괄 확정
                        """)
        @PostMapping("/monthly/finalize-all")
        public ResponseEntity<ApiResponse<Void>> finalizeAll(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                confirmFacade.finalizeAll(month);
                return ResponseEntity.ok(ApiResponse.success(null));
        }
}
