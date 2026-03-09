package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlySettlementRepository extends JpaRepository<MonthlySettlement, Long> {

    // 특정 월 전체 가맹점 (HQ 월별 정산 목록)
    List<MonthlySettlement> findAllBySettlementMonth(YearMonth month);

    // 특정 가맹점 + 월 (가맹점 월별 요약)
    Optional<MonthlySettlement> findByFranchiseIdAndSettlementMonth(
            Long franchiseId, YearMonth month);

    // 상태별 카운트 (HQ 정산 확정 페이지: 작성중 n개, 확정요청 n개, 최종확정 n개)
    long countBySettlementMonthAndStatus(YearMonth month, SettlementStatus status);
}
