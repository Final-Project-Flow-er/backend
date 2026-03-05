package com.chaing.api.controller.franchise;

import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.VoucherType;
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
import java.time.YearMonth;
import java.util.List;

@RestController
@Tag(name = "FranchiseSettlement API", description = "가맹점 정산 관련 API")
@RequestMapping("/api/v1/franchise/settlements")
@RequiredArgsConstructor
public class FranchiseSettlementController {

    //일별
    @Operation(summary = "일별 정산 요약 조회", description = "일별 정산 요약(최총정산금액, 총매출, 반품환급, 발주대금, ,배송비, 손실, 수수료)")
    @GetMapping("/daily/summary")
    public ResponseEntity<ApiResponse<?>> getDailySummary(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "일별 매출 현황 조회", description = "매출 현황 리스트(상품명, 수량, 단가, 총매출)")
    @GetMapping("/daily/sales-items")
    public ResponseEntity<ApiResponse<?>> getDailySalesItems(
            @RequestParam("date") @DateTimeFormat(iso =  DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "일별 발주 내역 조회", description = "발주 내역 리스트(상품명/수량/단가/총금액)")
    @GetMapping("/daily/orders-items")
    public ResponseEntity<ApiResponse<?>> getDailyOrdersItems(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    //월별
    @Operation(summary = "월별 정산 요약 조회", description = "월별 정산 요약(최총정산금액, 총매출, 반품환급, 발주대금, 배송비, 손실, 수수료)")
    @GetMapping("/monthly/summary")
    public ResponseEntity<ApiResponse<?>> getMonthlySummary(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "월별 매출 상위 조회", description = "월별 상품별 매출 전체조회, limit있으면 매출기준 상위 5개 조회")
    @GetMapping("/monthly/sales-items")
    public ResponseEntity<ApiResponse<?>> getMonthlySales(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        if (limit != null && limit <1) {
            throw new IllegalArgumentException("limit는 0보다 커야합니다");
        }
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @Operation(summary = "월별 일자 매출 추이 조회 그래프", description = "기간 선택 후 매출 추이 그래프 조회")
    @GetMapping("/monthly/daily-sales-graph")
    public ResponseEntity<ApiResponse<?>> getMonthlyDailySalesGraph(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @Operation(summary = "월별 발주 내역 상위 조회", description = "월별 발주 상품별 전체조회, limit있으면 수량기준 상위 5개 조회")
    @GetMapping("/monthly/order-items")
    public ResponseEntity<ApiResponse<?>> getMonthlyOrders(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        if (limit != null && limit <1) {
            throw new IllegalArgumentException("limit는 0보다 커야합니다");
        }
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    // 전표 상세 목록 조회, 일/월 공통
    @Operation(summary = "전표 상세 목록 조회(일/월 공통)",
                description = """
                        전표 페이지1개 period, date, month, type으로 목록 필터링
                        - period=DAILY, date 필수
                        - period=MONTHLY, month 필수
                        - type 없으면 전체
                        """
    )
    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<?>> getVouchers(
            @RequestParam("period") PeriodType period,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "month", required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "type", required = false) VoucherType type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));

    }

    //pdf, excel 다운로드
    @Operation(summary = "일별 정산 영수증 PDF 조회", description = "일별 정산 영수증 pdf 조회")
    @GetMapping("/daily/receipt/pdf")
    public ResponseEntity<ApiResponse<?>> getDailyReceiptPdf(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @Operation(summary = "월별 정산 영수증 PDF 조회", description = "월별 정산 영수증 pdf 조회")
    @GetMapping("/monthly/receipt/pdf")
    public ResponseEntity<ApiResponse<?>> getMonthlyReceiptPdf(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @Operation(summary = "월별 전표 Excel 조회", description = "월별 전표 Excel 조회")
    @GetMapping("/monthly/vouchers/excel")
    public ResponseEntity<ApiResponse<?>> getMonthlyVouchersExcel(
            @RequestParam("month") @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(value = "type", required = false) VoucherType type
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

}

