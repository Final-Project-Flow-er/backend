package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.SettlementDocument;
import com.chaing.domain.settlements.exception.SettlementErrorCode;
import com.chaing.domain.settlements.exception.SettlementException;
import com.chaing.domain.settlements.repository.interfaces.DailySettlementReceiptRepository;
import com.chaing.domain.settlements.repository.interfaces.SettlementDocumentRepository;
import com.chaing.domain.settlements.service.SettlementDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementDocumentServiceImpl implements SettlementDocumentService {

    private final SettlementDocumentRepository documentRepository;
    private final DailySettlementReceiptRepository dailyReceiptRepository;

    @Override
    public SettlementDocument getDailyDocument(Long dailyReceiptId) {
        return documentRepository.findByDailyReceiptId(dailyReceiptId)
                .orElseThrow(() -> {
                    // 문서가 없을 때 원인 분석
                    if (!dailyReceiptRepository.existsById(dailyReceiptId)) {
                        // 상황 3: 정산 ID 자체가 유효하지 않음
                        return new SettlementException(SettlementErrorCode.INVALID_SETTLEMENT_ID);
                    }
                    // 상황 1: 정산 데이터는 있으나 문서가 아직 생성되지 않음
                    return new SettlementException(SettlementErrorCode.DOCUMENT_STILL_GENERATING);
                });
    }

    @Override
    public List<SettlementDocument> getMonthlyDocuments(Long monthlySettlementId) {
        return documentRepository.findAllByMonthlySettlementId(monthlySettlementId);
    }

    @Override
    public SettlementDocument getHQDailyDocument(java.time.LocalDate date) {
        return documentRepository
                .findByDocumentOwnerAndSettlementDate(com.chaing.domain.settlements.enums.DocumentOwner.HQ, date)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.DOCUMENT_STILL_GENERATING));
    }

    @Override
    public SettlementDocument getHQMonthlyDocument(java.time.YearMonth month) {
        return documentRepository
                .findByDocumentOwnerAndSettlementMonth(com.chaing.domain.settlements.enums.DocumentOwner.HQ, month)
                .orElseThrow(() -> new SettlementException(SettlementErrorCode.DOCUMENT_STILL_GENERATING));
    }

    @Override
    @Transactional
    public void save(SettlementDocument document) {
        log.info("[DEBUG] Saving SettlementDocument: type={}, owner={}",
                document.getDocumentType(), document.getDocumentOwner());
        documentRepository.save(document);
    }
}
