package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.SettlementDocument;
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

    @Override
    public java.util.Optional<SettlementDocument> getDailyDocument(Long dailyReceiptId) {
        return documentRepository.findByDailyReceiptId(dailyReceiptId);
    }

    @Override
    public List<SettlementDocument> getMonthlyDocuments(Long monthlySettlementId) {
        return documentRepository.findAllByMonthlySettlementId(monthlySettlementId);
    }

    @Override
    public java.util.Optional<SettlementDocument> getHQDailyDocument(java.time.LocalDate date) {
        return documentRepository
                .findByDocumentOwnerAndSettlementDate(com.chaing.domain.settlements.enums.DocumentOwner.HQ, date);
    }

    @Override
    public java.util.Optional<SettlementDocument> getHQMonthlyDocument(java.time.YearMonth month) {
        return documentRepository
                .findByDocumentOwnerAndSettlementMonth(com.chaing.domain.settlements.enums.DocumentOwner.HQ, month);
    }

    @Override
    @Transactional
    public void save(SettlementDocument document) {
        log.info("[DEBUG] Saving SettlementDocument: type={}, owner={}",
                document.getDocumentType(), document.getDocumentOwner());
        documentRepository.save(document);
    }
}
