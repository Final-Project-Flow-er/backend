package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.entity.SettlementVoucher;

import java.util.List;

public interface SettlementFileService {

    // 일별 정산 영수증 PDF 생성

    byte[] createDailyReceiptPdf(DailySettlementReceipt receipt, List<DailyReceiptLine> lines);

    /**
     * 월별 정산 내역 엑셀 생성 (본사/요약용)
     */
    byte[] createMonthlySettlementExcel(List<MonthlySettlement> settlements);

    /**
     * 월별 전표 상세 내역 엑셀 생성 (가맹점/상세용)
     */
    byte[] createMonthlyVoucherExcel(List<SettlementVoucher> vouchers);

    /**
     * 월별 정산 영수증 PDF 생성
     */
    byte[] createMonthlyReceiptPdf(MonthlySettlement settlement, List<SettlementVoucher> vouchers);

    byte[] createHQSettlementDailyPdf(java.time.LocalDate date, List<DailySettlementReceipt> receipts);

    byte[] createHQSettlementMonthlyPdf(java.time.YearMonth month, List<MonthlySettlement> settlements);
}
