package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.MonthlySettlement;
import com.chaing.domain.settlements.enums.SettlementStatus;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
import com.chaing.domain.settlements.repository.interfaces.MonthlySettlementRepository;
import com.chaing.domain.settlements.service.MonthlySettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlySettlementServiceImpl implements MonthlySettlementService {

    private final MonthlySettlementRepository repository;

    @Override
    public List<MonthlySettlement> getAllByMonth(YearMonth month, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return repository.findAllBySettlementMonthAndFranchiseNameContaining(month, keyword);
        }
        return repository.findAllBySettlementMonth(month);
    } // 본사에서 해당'월'의 가맹점 정사 조회할 때

    @Override
    public MonthlySettlement getByFranchiseAndMonth(Long franchiseId, YearMonth month) {
        return repository.findByFranchiseIdAndSettlementMonth(franchiseId, month)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND));
    } // 가맹점이 해당 '월'의 정산을 조회할 때

    @Override
    public Map<String, Long> getStatusCounts(YearMonth month) {
        return Map.of(
                "CALCULATED", repository.countBySettlementMonthAndStatus(month, SettlementStatus.CALCULATED),
                "CONFIRM_REQUESTED",
                repository.countBySettlementMonthAndStatus(month, SettlementStatus.CONFIRM_REQUESTED),
                "CONFIRMED", repository.countBySettlementMonthAndStatus(month, SettlementStatus.CONFIRMED));
    } // 본사에서 작성중/확정 등 몇개인지

    @Override
    @Transactional
    public MonthlySettlement requestConfirm(Long monthlySettlementId) {
        MonthlySettlement settlement = repository.findById(monthlySettlementId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND));
        settlement.requestConfirm(); // 상태 변경: CALCULATED → CONFIRM_REQUESTED
        return repository.save(settlement);
    } // 상태 : '확정 요청'으로

    @Override
    @Transactional
    public MonthlySettlement confirm(Long monthlySettlementId) {
        MonthlySettlement settlement = repository.findById(monthlySettlementId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND));
        settlement.confirm(); // 상태 변경: CONFIRM_REQUESTED → CONFIRMED
        return repository.save(settlement);
    } // 상태 : '최종 확정'으로

    @Override
    @Transactional
    public MonthlySettlement rollback(Long monthlySettlementId) {
        MonthlySettlement settlement = repository.findById(monthlySettlementId)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.MONTHLY_SETTLEMENT_NOT_FOUND));
        settlement.rollback(); // 상태 변경: CONFIRM_REQUESTED → CALCULATED
        return repository.save(settlement);
    } // 상태 : 수정 누르면 그 전으로 rollback

}
