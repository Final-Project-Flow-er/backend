package com.chaing.api.facade.settlement;

import com.chaing.api.dto.franchise.settlement.response.FranchiseSettlementSummaryResponse;
import com.chaing.api.dto.franchise.settlement.response.FranchiseVoucherResponse;
import com.chaing.api.dto.hq.settlement.request.*;
import com.chaing.api.dto.hq.settlement.response.*;
import com.chaing.domain.settlements.enums.PeriodType;
import com.chaing.domain.settlements.enums.VoucherType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementFacade {

    // 향후 실 데이터를 가져오기 위해 주입될 Service들 (예: HQSummaryService 등)
    // private final HQSummaryService summaryService;

    // ---------------------------------------------------------
    // 1. 일별 (Daily) 조회
    // ---------------------------------------------------------
    public HQSettlementSummaryResponse getDailySummary(HQSettlementDailySummaryRequest request) {
        // TODO: 실제 로직 구현
        return null;
    }

    public Page<HQFranchiseSettlementResponse> getDailyFranchises(HQSettlementDailyFranchisesRequest request) {
        // TODO: 실제 로직 구현
        return Page.empty();
    }

    public List<HQDailyGraphResponse> getDailyTrend(HQSettlementDailyGraphRequest request) {
        // TODO: 실제 로직 구현
        return List.of();
    }

    // ---------------------------------------------------------
    // 2. 월별 (Monthly) 조회
    // ---------------------------------------------------------
    public HQSettlementSummaryResponse getMonthlySummary(HQSettlementMonthlySummaryRequest request) {
        // TODO: 실제 로직 구현
        return null;
    }

    public Page<HQFranchiseSettlementResponse> getMonthlyFranchises(HQSettlementMonthlyFranchisesRequest request) {
        // TODO: 실제 로직 구현
        return Page.empty();
    }

    public List<HQMonthlyGraphResponse> getMonthlyTrend(HQSettlementMonthlyGraphRequest request) {
        // TODO: 실제 로직 구현
        return List.of();
    }

    // ---------------------------------------------------------
    // 3. 단건 가맹점 상세 내역 (Drill-down) & 전표 조회
    // ---------------------------------------------------------
    public FranchiseSettlementSummaryResponse getDailyFranchiseSummary(Long franchiseId,
            HQSettlementFranchiseDailyDetailRequest request) {
        return null;
    }

    public FranchiseSettlementSummaryResponse getMonthlyFranchiseSummary(Long franchiseId,
            HQSettlementFranchiseMonthlyDetailRequest request) {
        return null;
    }

    public Page<FranchiseVoucherResponse> getFranchiseVouchers(Long franchiseId, PeriodType period, LocalDate date,
            YearMonth month, VoucherType type, int page, int size) {
        return Page.empty();
    }

    // ---------------------------------------------------------
    // 4. PDF 및 엑셀 다운로드 (URL 반환)
    // ---------------------------------------------------------
    public String getDailyAllSummaryPdf(HQSettlementDailyAllPdfRequest request) {
        return "https://dummy-url.com/daily-all-summary.pdf";
    }

    public String getMonthlyAllSummaryPdf(HQSettlementMonthlyAllPdfRequest request) {
        return "https://dummy-url.com/monthly-all-summary.pdf";
    }

    public String getDailyFranchiseReceiptPdf(Long franchiseId, HQSettlementFranchiseDailyReceiptPdfRequest request) {
        return "https://dummy-url.com/daily-franchise-receipt.pdf";
    }

    public String getMonthlyFranchiseReceiptPdf(Long franchiseId,
            HQSettlementFranchiseMonthlyReceiptPdfRequest request) {
        return "https://dummy-url.com/monthly-franchise-receipt.pdf";
    }

    public String getMonthlyExcel(HQSettlementMonthlyExcelRequest request) {
        return "https://dummy-url.com/monthly-excel.xlsx";
    }
}
