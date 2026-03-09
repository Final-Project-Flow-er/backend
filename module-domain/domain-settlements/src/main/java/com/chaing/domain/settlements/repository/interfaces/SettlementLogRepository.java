package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.SettlementLog;
import com.chaing.domain.settlements.enums.SettlementLogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementLogRepository extends JpaRepository<SettlementLog, Long> {
    // 전체 로그 (페이징)
    Page<SettlementLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 유형별 필터 (정산확정/문서생성/조정전표/취소 탭)
    Page<SettlementLog> findAllByTypeOrderByCreatedAtDesc(
            SettlementLogType type, Pageable pageable);

}
