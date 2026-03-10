package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.SettlementLog;
import com.chaing.domain.settlements.enums.SettlementLogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementLogRepository extends JpaRepository<SettlementLog, Long> {
    // 전체 로그 (페이징)
    Page<SettlementLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 유형별 필터 (정산확정/문서생성/조정전표/취소 탭)
    Page<SettlementLog> findAllByTypeOrderByCreatedAtDesc(
            SettlementLogType type, Pageable pageable);

    @Query("SELECT l FROM SettlementLog l " +
            "WHERE (:franchiseId IS NULL OR l.franchiseId = :franchiseId) " +
            "AND (:type IS NULL OR l.type = :type) " +
            "ORDER BY l.createdAt DESC")
    Page<SettlementLog> findByConditions(
            @Param("franchiseId") Long franchiseId,
            @Param("type") SettlementLogType type,
            Pageable pageable);

}
