package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.SettlementAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementAdjustmentService {

    // 조정 전표 생성 (+ 누르고 작성)
    SettlementAdjustment create(SettlementAdjustment adjustment);

    // 목록 조회 (페이징)
    Page<SettlementAdjustment> getAll(Pageable pageable);

    // 단건 조회 (하단 상세)
    SettlementAdjustment getById(Long id);
}
