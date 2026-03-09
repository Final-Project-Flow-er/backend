package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.SettlementLog;
import com.chaing.domain.settlements.enums.SettlementLogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementLogService {

    // 로그 생성
    SettlementLog create(SettlementLog log);

    // 로그 조회
    Page<SettlementLog> getAll(Pageable pageable);

    // 유형별 필터 조회
    Page<SettlementLog> getAllByType(SettlementLogType type, Pageable pageable);
}
