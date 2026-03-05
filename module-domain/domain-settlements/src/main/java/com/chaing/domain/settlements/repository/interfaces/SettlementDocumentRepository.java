package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.SettlementDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementDocumentRepository extends JpaRepository<SettlementDocument, Long> {
    // 월별 정산의 문서 목록
    List<SettlementDocument> findAllByMonthlySettlementId(Long monthlySettlementId);

    // 일별 영수증의 문서
    Optional<SettlementDocument> findByDailyReceiptId(Long dailyReceiptId);
}
