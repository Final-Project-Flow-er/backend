package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.SettlementDocument;
import java.util.List;

public interface SettlementDocumentService {
    // 1. 일별 영수증 문서 단건 조회
    SettlementDocument getDailyDocument(Long dailyReceiptId);

    // 2. 월별 정산 문서 목록 조회
    List<SettlementDocument> getMonthlyDocuments(Long monthlySettlementId);

    // 3. 본사 통합 문서 조회
    SettlementDocument getHQDailyDocument(java.time.LocalDate date);

    SettlementDocument getHQMonthlyDocument(java.time.YearMonth month);

    // 4. 문서 메타데이터 저장
    void save(SettlementDocument document);
}
