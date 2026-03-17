package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.enums.VoucherType;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
import com.chaing.domain.settlements.repository.interfaces.DailyReceiptLineRepository;
import com.chaing.domain.settlements.repository.interfaces.DailySettlementReceiptRepository;
import com.chaing.domain.settlements.service.DailySettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailySettlementServiceImpl implements DailySettlementService {

    private final DailySettlementReceiptRepository receiptRepository;
    private final DailyReceiptLineRepository lineRepository;

    @Override
    public List<DailySettlementReceipt> getAllByDate(LocalDate date, String keyword) {
        log.info("[DEBUG] getAllByDate called with date: {}, keyword: {}", date, keyword);
        List<DailySettlementReceipt> results;
        if (keyword != null && !keyword.trim().isEmpty()) {
            results = receiptRepository.findAllBySettlementDateAndFranchiseNameContaining(date, keyword);
        } else {
            results = receiptRepository.findAllBySettlementDate(date);
        }
        log.info("[DEBUG] getAllByDate found {} receipts", results.size());
        return results;
    }

    @Override
    public DailySettlementReceipt getByFranchiseAndDate(Long franchiseId, LocalDate date) {
        log.info("[DEBUG] getByFranchiseAndDate called for franchiseId: {}, date: {}", franchiseId, date);
        return receiptRepository.findByFranchiseIdAndSettlementDate(franchiseId, date)
                .orElseThrow(() -> {
                    log.warn("[DEBUG] Daily Settlement NOT FOUND for franchiseId: {}, date: {}", franchiseId, date);
                    return new SettlementException(SettlementErrorCode.DAILY_SETTLEMENT_NOT_FOUND);
                });
    } // 가맹점에서 정산을 조회하고 싶을 때

    @Override
    public List<DailySettlementReceipt> getAllByDateRange(LocalDate start, LocalDate end) {
        return receiptRepository.findAllBySettlementDateBetween(start, end);
    } // 본사 그래프

    @Override
    public List<DailySettlementReceipt> getAllByFranchiseAndDateRange(
            Long franchiseId, LocalDate start, LocalDate end) {
        return receiptRepository.findAllByFranchiseIdAndSettlementDateBetween(franchiseId, start, end);
    } // 가맹점 그래프

    @Override
    public Page<DailyReceiptLine> getReceiptLines(
            Long dailyReceiptId, VoucherType type, Pageable pageable) {
        if (type == null) {
            // 전체 탭
            return lineRepository.findAllByDailyReceiptId(dailyReceiptId, pageable);
        }
        // 유형별 필터 (판매/발주/배송/수수료/반품/손실)
        return lineRepository.findAllByDailyReceiptIdAndLineType(dailyReceiptId, type, pageable);
    } // 전표 상세 목록

    @Override
    public List<DailyReceiptLine> getAllReceiptLines(Long dailyReceiptId) {
        return lineRepository.findAllByDailyReceiptId(dailyReceiptId);
    } // Excel 다운로드
}
