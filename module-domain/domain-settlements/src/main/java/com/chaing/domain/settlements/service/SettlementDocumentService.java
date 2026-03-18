package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.SettlementDocument;
import java.util.List;
import java.util.Optional;

public interface SettlementDocumentService {
    // 1. 일별 영수증 문서 단건 조회
    Optional<SettlementDocument> getDailyDocument(Long dailyReceiptId);

    // 2. 월별 정산 문서 목록 조회
    List<SettlementDocument> getMonthlyDocuments(Long monthlySettlementId);

    // 3. 본사 통합 문서 조회
    Optional<SettlementDocument> getHQDailyDocument(java.time.LocalDate date);

    Optional<SettlementDocument> getHQMonthlyDocument(java.time.YearMonth month);

    // 4. 문서 메타데이터 저장
    void save(SettlementDocument document);

    // 5. 유령 레코드 삭제 (MinIO 파일 없는 stale 레코드 정리용)
    void deleteById(Long settlementDocumentId);
}
