package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.SettlementAdjustment;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
import com.chaing.domain.settlements.repository.interfaces.SettlementAdjustmentRepository;
import com.chaing.domain.settlements.service.SettlementAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementAdjustmentServiceImpl implements SettlementAdjustmentService {

    private final SettlementAdjustmentRepository repository;

    @Override
    @Transactional
    public SettlementAdjustment create(SettlementAdjustment adjustment) {
        try {
            return repository.save(adjustment);
        } catch (DataIntegrityViolationException e) {
            throw new SettlementException(SettlementErrorCode.INVALID_ADJUSTMENT_DATA);
        }
    } // 새로운 조정 내역을 DB에 저장

    @Override
    public Page<SettlementAdjustment> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    } // 조정 내역 DB에서 가져오기

    @Override
    public SettlementAdjustment getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.ADJUSTMENT_NOT_FOUND));
    } // 특정 조정 내역 DB에서 가져오기
}
