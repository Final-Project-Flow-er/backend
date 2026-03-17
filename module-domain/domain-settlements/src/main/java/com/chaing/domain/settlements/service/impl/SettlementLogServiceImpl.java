package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.SettlementLog;
import com.chaing.domain.settlements.enums.SettlementLogType;
import com.chaing.domain.settlements.repository.interfaces.SettlementLogRepository;
import com.chaing.domain.settlements.service.SettlementLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementLogServiceImpl implements SettlementLogService {

    private final SettlementLogRepository repository;

    @Override
    @Transactional
    public SettlementLog create(SettlementLog log) {
        return repository.save(log);
    } // DB에 저장

    @Override
    public Page<SettlementLog> getAll(Pageable pageable) {
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    } // 최신순으로 조회

    @Override
    public Page<SettlementLog> getAllByType(SettlementLogType type, Pageable pageable) {
        return repository.findAllByTypeOrderByCreatedAtDesc(type, pageable);
    } // 특정 유형 필터링

    @Override
    public Page<SettlementLog> getAllByConditions(Long franchiseId, SettlementLogType type, Pageable pageable) {
        return repository.findByConditions(franchiseId, type, pageable);
    } // 본사에서 필요한 조건(가맹점, 로그 타입)으로 로그 내역 조회
}
