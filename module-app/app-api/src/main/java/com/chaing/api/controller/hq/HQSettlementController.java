package com.chaing.api.controller.hq;

import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.SettlementStatus;
import com.chaing.domain.settlements.enums.VoucherType;
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
import java.time.YearMonth;
import java.util.List;

@RestController
@Tag(name = "HQSettlement API", description = "본사 정산 관련 API")
@RequestMapping("/api/v1/hq/settlements")
@RequiredArgsConstructor
public class HQSettlementController {

        // 일별
        @Operation(summary = "본사 일별 정산 요약 조회(합산)", description = "본사 관점 일별 정산 요약(최종 정산 금액, 발주 매출, 수수료 수익, 배송 수익, 반품 차감액, 본사 손실)")
        @GetMapping("/daily/summary")
        public ResponseEntity<ApiResponse<?>> getDailySummary(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "본사 일별 가맹점별 정산 목록 조회", description = "선택한 일자에 대한 가맹점별 정산 목록(검색/상태 필터/페이징)")
        @GetMapping("/daily/franchises")
        public ResponseEntity<ApiResponse<?>> getDailyFranchiseSettlements(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(value = "keyword", required = false) String keyword, // 가맹점명 검색
                        @RequestParam(value = "status", required = false) SettlementStatus status, // 대기, 정산완료
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        @Operation(summary = "본사 일별 전체 가맹점 정산 추이(그래프)", description = "기간(start~end) 동안 전체 가맹점 합계 추이(일자별)")
        @GetMapping("/daily/daily-sales-graph")
        public ResponseEntity<ApiResponse<?>> getDailyTrend(
                        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        // 월별
        @Operation(summary = "본사 월별 정산 요약 조회(합산)", description = "본사 관점 월별 정산 요약(최종 정산 금액, 발주 매출, 수수료 수익, 배송 수익, 반품 차감액, 본사 손실)")
        @GetMapping("/monthly/summary")
        public ResponseEntity<ApiResponse<?>> getMonthlySummary(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "본사 월별 가맹점별 정산 목록 조회", description = "선택한 월에 대한 가맹점별 정산 목록(검색/상태 필터/페이징)")
        @GetMapping("/monthly/franchises")
        public ResponseEntity<ApiResponse<?>> getMonthlyFranchiseSettlements(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "status", required = false) SettlementStatus status,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        @Operation(summary = "본사 월별 전체 가맹점 정산 추이(그래프)", description = "기간(start~end) 동안 전체 가맹점 합계 추이(월별 또는 선택 월 내부 일자별은 일별 trend로 처리)")
        @GetMapping("/monthly/monthly-sales-graph")
        public ResponseEntity<ApiResponse<?>> getMonthlyTrend(
                        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                        @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        // 일별, 월별 가맹점 '상세', franchisedId를 path로 받음
        @Operation(summary = "본사 가맹점 일별 정산 상세 요약", description = "본사가 특정 가맹점(franchiseId)의 일별 정산 상세 요약을 조회")
        @GetMapping("/daily/franchises/{franchiseId}/summary")
        public ResponseEntity<ApiResponse<?>> getDailyFranchiseSummary(
                        @PathVariable Long franchiseId,
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "본사 가맹점 월별 정산 상세 요약", description = "본사가 특정 가맹점(franchiseId)의 월별 정산 상세 요약을 조회")
        @GetMapping("/monthly/franchises/{franchiseId}/summary")
        public ResponseEntity<ApiResponse<?>> getMonthlyFranchiseSummary(
                        @PathVariable Long franchiseId,
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        // 전표 상세 (본사에서 특정 가맹점 전표로 이동)
        @Operation(summary = "본사 전표 상세 목록 조회(가맹점 단건, 일/월 공통)", description = """
                        본사 -> 가맹점 전표 상세 목록 화면용
                        - franchiseId 필수
                        - period=DAILY면 date 필수
                        - period=MONTHLY면 month 필수
                        - type 없으면 전체
                        """)

        @GetMapping("/vouchers")
        public ResponseEntity<ApiResponse<?>> getFranchiseVouchersByHq(
                        @RequestParam("franchiseId") Long franchiseId,
                        @RequestParam("period") PeriodType period,
                        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @RequestParam(value = "type", required = false) VoucherType type,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        // pdf, excel 다운로드
        @Operation(summary = "본사 전체 가맹점 요약 PDF 조회(일별)", description = "선택한 일자 기준 전체 가맹점 요약 PDF")
        @GetMapping("/daily/receipt-all/pdf")
        public ResponseEntity<ApiResponse<?>> getDailyAllSummaryPdf(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "본사 전체 가맹점 요약 PDF 조회(월별)", description = "선택한 월 기준 전체 가맹점 요약 PDF")
        @GetMapping("/monthly/receipt-all/pdf")
        public ResponseEntity<ApiResponse<?>> getMonthlyAllSummaryPdf(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "본사 가맹점별 영수증 PDF 조회(일별)", description = "특정 가맹점의 일별 영수증 PDF(가맹점 화면의 '정산 영수증 다운로드'와 동일 문서)")
        @GetMapping("/daily/franchises/{franchiseId}/receipt/pdf")
        public ResponseEntity<ApiResponse<?>> getDailyFranchiseReceiptPdf(
                        @PathVariable Long franchiseId,
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "본사 가맹점별 영수증 PDF 조회(월별)", description = "특정 가맹점의 월별 영수증 PDF(가맹점 화면과 동일 문서)")
        @GetMapping("/monthly/franchises/{franchiseId}/receipt/pdf")
        public ResponseEntity<ApiResponse<?>> getMonthlyFranchiseReceiptPdf(
                        @PathVariable Long franchiseId,
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

        @Operation(summary = "본사 월별 전표 다운로드", description = "선택한 월 기준 본사 정산 Excel(요약/목록) 다운로드")
        @GetMapping("/monthly/vouchers/excel")
        public ResponseEntity<ApiResponse<?>> getMonthlyExcel(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @RequestParam(value = "type", required = false) VoucherType type) {
                return ResponseEntity.ok(ApiResponse.success(null));
        }

}
