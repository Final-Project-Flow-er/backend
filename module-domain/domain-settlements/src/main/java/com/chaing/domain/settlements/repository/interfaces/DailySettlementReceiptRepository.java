package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySettlementReceiptRepository extends JpaRepository<DailySettlementReceipt, Long> {

    // 특정 날짜 전체 가맹점 (본사 일별 정산 목록)
    List<DailySettlementReceipt> findAllBySettlementDate(LocalDate date);

    // 특정 날짜 특정 가맹점명(keyword) 검색 (본사 일별 정산 목록)
    @Query("SELECT d FROM DailySettlementReceipt d " +
            "JOIN Franchise f ON d.franchiseId = f.franchiseId " +
            "WHERE d.settlementDate = :date " +
            "AND f.name LIKE %:keyword%")
    List<DailySettlementReceipt> findAllBySettlementDateAndFranchiseNameContaining(
            @Param("date") LocalDate date,
            @Param("keyword") String keyword);

    // 특정 가맹점 + 특정 날짜 (가맹점 일별 요약)
    Optional<DailySettlementReceipt> findByFranchiseIdAndSettlementDate(Long franchiseId, LocalDate date);

    // 기간별 전체 (본사 그래프용)
    List<DailySettlementReceipt> findAllBySettlementDateBetween(LocalDate start, LocalDate end);

    // 특정 가맹점의 기간별 (가맹점 월별 매출 추이 그래프)
    List<DailySettlementReceipt> findAllByFranchiseIdAndSettlementDateBetween(
            Long franchiseId, LocalDate start, LocalDate end);

}
