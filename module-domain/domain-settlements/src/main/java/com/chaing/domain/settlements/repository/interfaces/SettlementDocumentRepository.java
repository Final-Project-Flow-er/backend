package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.SettlementDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettlementDocumentRepository extends JpaRepository<SettlementDocument, Long> {
    // 월별 정산의 문서 목록
    List<SettlementDocument> findAllByMonthlySettlementId(Long monthlySettlementId);

    // 일별 영수증의 문서
    Optional<SettlementDocument> findByDailyReceiptId(Long dailyReceiptId);

    // 본사 통합 문서 조회
    Optional<SettlementDocument> findByDocumentOwnerAndSettlementDate(
            com.chaing.domain.settlements.enums.DocumentOwner documentOwner, java.time.LocalDate settlementDate);

    Optional<SettlementDocument> findByDocumentOwnerAndSettlementMonth(
            com.chaing.domain.settlements.enums.DocumentOwner documentOwner, java.time.YearMonth settlementMonth);
}
