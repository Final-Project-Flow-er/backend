package com.chaing.api.controller.franchise;

import com.chaing.api.facade.franchise.FranchiseSettlementFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.VoucherType;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@Tag(name = "FranchiseSettlement API", description = "가맹점 정산 관련 API")
@RequestMapping("/api/v1/franchise/settlements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('FRANCHISE')")
public class FranchiseSettlementController {

        private final FranchiseSettlementFacade facade;

        // 일별
        @Operation(summary = "일별 정산 요약 조회", description = "일별 정산 요약(최총정산금액, 총매출, 반품환급, 발주대금, ,배송비, 손실, 수수료)")
        @GetMapping("/daily/summary")
        public ResponseEntity<ApiResponse<?>> getDailySummary(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getDailySummary(franchiseId, date)));
        }

        @Operation(summary = "일별 매출 현황 조회", description = "매출 현황 리스트(상품명, 수량, 단가, 총매출)")
        @GetMapping("/daily/sales-items")
        public ResponseEntity<ApiResponse<?>> getDailySalesItems(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(value = "limit", required = false) Integer limit,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getDailySalesItems(franchiseId, date, limit)));
        }

        @Operation(summary = "일별 발주 내역 조회", description = "발주 내역 리스트(상품명/수량/단가/총금액)")
        @GetMapping("/daily/order-items")
        public ResponseEntity<ApiResponse<?>> getDailyOrdersItems(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(value = "limit", required = false) Integer limit,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getDailyOrderItems(franchiseId, date, limit)));
        }

        // 월별
        @Operation(summary = "월별 정산 요약 조회", description = "월별 정산 요약(최총정산금액, 총매출, 반품환급, 발주대금, 배송비, 손실, 수수료)")
        @GetMapping("/monthly/summary")
        public ResponseEntity<ApiResponse<?>> getMonthlySummary(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getMonthlySummary(franchiseId, month)));
        }

        @Operation(summary = "월별 매출 상위 조회", description = "월별 상품별 매출 전체조회, limit있으면 매출기준 상위 5개 조회")
        @GetMapping("/monthly/sales-items")
        public ResponseEntity<ApiResponse<?>> getMonthlySales(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @RequestParam(value = "limit", required = false) Integer limit,
                        @AuthenticationPrincipal UserPrincipal principal) {
                if (limit != null && limit < 1) {
                        throw new SettlementException(SettlementErrorCode.INVALID_PARAMETER);
                }
                Long franchiseId = principal.getBusinessUnitId();
                // TODO: Swagger 테스트용 임시 응답. 추후 서비스 로직 연동 예정
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getMonthlySalesItems(franchiseId, month, limit)));
        }

        @Operation(summary = "월별 발주 내역 상위 조회", description = "월별 발주 상품별 전체조회, limit있으면 수량기준 상위 5개 조회")
        @GetMapping("/monthly/order-items")
        public ResponseEntity<ApiResponse<?>> getMonthlyOrders(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @RequestParam(value = "limit", required = false) Integer limit,
                        @AuthenticationPrincipal UserPrincipal principal) {
                if (limit != null && limit < 1) {
                        throw new SettlementException(SettlementErrorCode.INVALID_PARAMETER);
                }
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getMonthlyOrderItems(franchiseId, month, limit)));
        }

        @Operation(summary = "월별 매출 추이 조회(그래프)", description = "월별 일자별 매출 합계 리스트 조회")
        @GetMapping("/monthly/sales-graph")
        public ResponseEntity<ApiResponse<?>> getMonthlySalesTrend(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getMonthlySalesTrend(franchiseId, month)));
        }

        // 전표 상세 목록 조회, 일/월 공통
        @Operation(summary = "전표 상세 목록 조회(일/월 공통)", description = """
                        전표 페이지1개 period, date, month, type으로 목록 필터링
                        - period=DAILY, date 필수
                        - period=MONTHLY, month 필수
                        - type 없으면 전체
                        """)
        @GetMapping("/vouchers")
        public ResponseEntity<ApiResponse<?>> getVouchers(
                        @RequestParam("period") PeriodType period,
                        @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(value = "month", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @RequestParam(value = "type", required = false) VoucherType type,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "20") int size,
                        @AuthenticationPrincipal UserPrincipal principal) {
                if (page < 0 || size < 1) {
                        throw new SettlementException(SettlementErrorCode.INVALID_PAGINATION);
                }
                if (period == PeriodType.DAILY && date == null) {
                        throw new SettlementException(SettlementErrorCode.INVALID_PARAMETER);
                }
                if (period == PeriodType.MONTHLY && month == null) {
                        throw new SettlementException(SettlementErrorCode.INVALID_PARAMETER);
                }
                Long franchiseId = principal.getBusinessUnitId();
                Pageable pageable = PageRequest.of(page, size);
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getVouchers(franchiseId, period, date, month, type, pageable)));

        }

        // pdf, excel 다운로드
        @Operation(summary = "일별 정산 영수증 PDF 조회", description = "일별 정산 영수증 pdf 조회")
        @GetMapping("/daily/receipt/pdf")
        public ResponseEntity<ApiResponse<?>> getDailyReceiptPdf(
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getDailyReceiptPdf(franchiseId, date)));
        }

        @Operation(summary = "월별 정산 영수증 PDF 조회", description = "월별 정산 영수증 pdf 조회")
        @GetMapping("/monthly/receipt/pdf")
        public ResponseEntity<ApiResponse<?>> getMonthlyReceiptPdf(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getMonthlyReceiptPdf(franchiseId, month)));
        }

        @Operation(summary = "월별 전표 Excel 조회", description = "월별 전표 Excel 조회")
        @GetMapping("/monthly/vouchers/excel")
        public ResponseEntity<ApiResponse<?>> getMonthlyVouchersExcel(
                        @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
                        @AuthenticationPrincipal UserPrincipal principal) {
                Long franchiseId = principal.getBusinessUnitId();
                return ResponseEntity.ok(ApiResponse.success(
                                facade.getMonthlyVouchersExcel(franchiseId, month)));
        }

}
