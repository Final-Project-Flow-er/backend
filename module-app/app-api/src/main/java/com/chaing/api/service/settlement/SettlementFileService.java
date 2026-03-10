package com.chaing.api.service.settlement;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.entity.MonthlySettlement;

import java.util.List;

public interface SettlementFileService {
    /**
     * 일별 정산 영수증 PDF 생성
     */
    byte[] createDailyReceiptPdf(DailySettlementReceipt receipt, List<DailyReceiptLine> lines);

    /**
     * 월별 정산 내역 엑셀 생성
     */
    byte[] createMonthlySettlementExcel(List<MonthlySettlement> settlements);
}
