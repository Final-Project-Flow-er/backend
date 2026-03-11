package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.settlement.request.*;
import com.chaing.api.dto.hq.settlement.response.*;
import com.chaing.api.dto.franchise.settlement.response.FranchiseSettlementSummaryResponse;
import com.chaing.api.dto.franchise.settlement.response.FranchiseVoucherResponse;
import com.chaing.api.facade.hq.HQSettlementFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.VoucherType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

import com.chaing.domain.settlements.entity.SettlementDocument;
import com.chaing.domain.settlements.service.SettlementDocumentService;

@RestController
@Tag(name = "HQSettlement API", description = "본사 정산 통합 컨트롤러 API")
@RequestMapping("/api/v1/hq/settlements")
@RequiredArgsConstructor
public class HQSettlementController {

        private final SettlementDocumentService documentService;
        private final HQSettlementFacade hqFacade;

        @Operation(summary = "일별 정산 영수증 문서 조회", description = "dailyReceiptId로 해당하는 문서를 가져옵니다.")
        @GetMapping("/daily-documents/{dailyReceiptId}")
        public ResponseEntity<ApiResponse<SettlementDocument>> getDailyDocument(
                        @PathVariable("dailyReceiptId") Long dailyReceiptId) {
                SettlementDocument document = documentService.getDailyDocument(dailyReceiptId);
                return ResponseEntity.ok(ApiResponse.success(document));
        }

        @Operation(summary = "월별 정산 문서 목록 조회", description = "monthlySettlementId로 해당하는 문서 목록을 가져옵니다.")
        @GetMapping("/monthly-documents/{monthlySettlementId}")
        public ResponseEntity<ApiResponse<List<SettlementDocument>>> getMonthlyDocuments(
                        @PathVariable("monthlySettlementId") Long monthlySettlementId) {
                List<SettlementDocument> documents = documentService.getMonthlyDocuments(monthlySettlementId);
                return ResponseEntity.ok(ApiResponse.success(documents));
        }

        // 일별
        @Operation(summary = "본사 일별 정산 요약 조회(합산)", description = "본사 관점 일별 정산 요약(최종 정산 금액, 발주 매출, 수수료 수익, 배송 수익, 반품 차감액, 본사 손실)")
        @GetMapping("/daily/summary")
        public ResponseEntity<ApiResponse<HQSettlementSummaryResponse>> getDailySummary(
                        @Valid HQSettlementDailySummaryRequest request) {
                HQSettlementSummaryResponse response = hqFacade.getDailySummary(request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "본사 일별 가맹점별 정산 목록 조회", description = "선택한 일자에 대한 가맹점별 정산 목록(검색/상태 필터/페이징)")
        @GetMapping("/daily/franchises")
        public ResponseEntity<ApiResponse<Page<HQFranchiseSettlementResponse>>> getDailyFranchiseSettlements(
                        @Valid HQSettlementDailyFranchisesRequest request) {
                Page<HQFranchiseSettlementResponse> response = hqFacade.getDailyFranchises(request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "본사 일별 전체 가맹점 정산 추이(그래프)", description = "기간(start~end) 동안 전체 가맹점 합계 추이(일자별)")
        @GetMapping("/daily/daily-sales-graph")
        public ResponseEntity<ApiResponse<List<HQDailyGraphResponse>>> getDailyTrend(
                        @Valid HQSettlementDailyGraphRequest request) {
                List<HQDailyGraphResponse> response = hqFacade.getDailyTrend(request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        // 월별

        @Operation(summary = "본사 월별 정산 요약 조회(합산)", description = "본사 관점 월별 정산 요약(최종 정산 금액, 발주 매출, 수수료 수익, 배송 수익, 반품 차감액, 본사 손실)")
        @GetMapping("/monthly/summary")
        public ResponseEntity<ApiResponse<HQSettlementSummaryResponse>> getMonthlySummary(
                        @Valid HQSettlementMonthlySummaryRequest request) {
                HQSettlementSummaryResponse response = hqFacade.getMonthlySummary(request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "본사 월별 가맹점별 정산 목록 조회", description = "선택한 월에 대한 가맹점별 정산 목록(검색/상태 필터/페이징)")
        @GetMapping("/monthly/franchises")
        public ResponseEntity<ApiResponse<Page<HQFranchiseSettlementResponse>>> getMonthlyFranchiseSettlements(
                        @Valid HQSettlementMonthlyFranchisesRequest request) {
                Page<HQFranchiseSettlementResponse> response = hqFacade.getMonthlyFranchises(request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "본사 월별 전체 가맹점 정산 추이(그래프)", description = "기간(start~end) 동안 전체 가맹점 합계 추이(월별 또는 선택 월 내부 일자별은 일별 trend로 처리)")
        @GetMapping("/monthly/monthly-sales-graph")
        public ResponseEntity<ApiResponse<List<HQMonthlyGraphResponse>>> getMonthlyTrend(
                        @Valid HQSettlementMonthlyGraphRequest request) {
                List<HQMonthlyGraphResponse> response = hqFacade.getMonthlyTrend(request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        // 상세 내역 (Drill-down)

        @Operation(summary = "본사 가맹점 일별 정산 상세 요약", description = "본사가 특정 가맹점(franchiseId)의 일별 정산 상세 요약을 조회")
        @GetMapping("/daily/franchises/{franchiseId}/summary")
        public ResponseEntity<ApiResponse<FranchiseSettlementSummaryResponse>> getDailyFranchiseSummary(
                        @PathVariable Long franchiseId,
                        @Valid HQSettlementFranchiseDailyDetailRequest request) {
                FranchiseSettlementSummaryResponse response = hqFacade.getDailyFranchiseSummary(franchiseId, request);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @Operation(summary = "본사 가맹점 월별 정산 상세 요약", description = "본사가 특정 가맹점(franchiseId)의 월별 정산 상세 요약을 조회")
        @GetMapping("/monthly/franchises/{franchiseId}/summary")
        public ResponseEntity<ApiResponse<FranchiseSettlementSummaryResponse>> getMonthlyFranchiseSummary(
                        @PathVariable Long franchiseId,
                        @Valid HQSettlementFranchiseMonthlyDetailRequest request) {
                FranchiseSettlementSummaryResponse response = hqFacade.getMonthlyFranchiseSummary(franchiseId, request);
                return ResponseEntity.ok(ApiResponse.success(response));
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
        public ResponseEntity<ApiResponse<Page<FranchiseVoucherResponse>>> getFranchiseVouchersByHq(
                        @RequestParam("franchiseId") Long franchiseId,
                        @RequestParam("period") PeriodType period,
                        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @RequestParam(value = "type", required = false) VoucherType type,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size) {
                Page<FranchiseVoucherResponse> response = hqFacade.getFranchiseVouchers(franchiseId, period, date,
                                month, type, page, size);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        // PDF 및 엑셀 다운로드 (URL 반환)

        @Operation(summary = "본사 전체 가맹점 요약 PDF 조회(일별)", description = "선택한 일자 기준 전체 가맹점 요약 PDF")
        @GetMapping("/daily/receipt-all/pdf")
        public ResponseEntity<ApiResponse<String>> getDailyAllSummaryPdf(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                String url = hqFacade.getDailyAllSummaryPdf(new HQSettlementDailyAllPdfRequest(date));
                return ResponseEntity.ok(ApiResponse.success(url));
        }

        @Operation(summary = "본사 전체 가맹점 요약 PDF 조회(월별)", description = "선택한 월 기준 전체 가맹점 요약 PDF")
        @GetMapping("/monthly/receipt-all/pdf")
        public ResponseEntity<ApiResponse<String>> getMonthlyAllSummaryPdf(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
                String url = hqFacade.getMonthlyAllSummaryPdf(new HQSettlementMonthlyAllPdfRequest(month));
                return ResponseEntity.ok(ApiResponse.success(url));
        }

        @Operation(summary = "본사 가맹점별 영수증 PDF 조회(일별)", description = "특정 가맹점의 일별 영수증 PDF(가맹점 화면의 '정산 영수증 다운로드'와 동일 문서)")
        @GetMapping("/daily/franchises/{franchiseId}/receipt/pdf")
        public ResponseEntity<ApiResponse<String>> getDailyFranchiseReceiptPdf(
                        @PathVariable Long franchiseId,
                        @Valid HQSettlementFranchiseDailyReceiptPdfRequest request) {
                String url = hqFacade.getDailyFranchiseReceiptPdf(franchiseId, request);
                return ResponseEntity.ok(ApiResponse.success(url));
        }

        @Operation(summary = "본사 가맹점별 영수증 PDF 조회(월별)", description = "특정 가맹점의 월별 영수증 PDF(가맹점 화면과 동일 문서)")
        @GetMapping("/monthly/franchises/{franchiseId}/receipt/pdf")
        public ResponseEntity<ApiResponse<String>> getMonthlyFranchiseReceiptPdf(
                        @PathVariable Long franchiseId,
                        @Valid HQSettlementFranchiseMonthlyReceiptPdfRequest request) {
                String url = hqFacade.getMonthlyFranchiseReceiptPdf(franchiseId, request);
                return ResponseEntity.ok(ApiResponse.success(url));
        }

        @Operation(summary = "본사 월별 전표 다운로드", description = "선택한 월 기준 본사 정산 Excel(요약/목록) 다운로드")
        @GetMapping("/monthly/vouchers/excel")
        public ResponseEntity<ApiResponse<String>> getMonthlyExcel(
                        @Valid HQSettlementMonthlyExcelRequest request) {
                String url = hqFacade.getMonthlyExcel(request);
                return ResponseEntity.ok(ApiResponse.success(url));
        }

}
