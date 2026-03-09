package com.chaing.domain.settlements.service.impl;

import com.chaing.domain.settlements.entity.SettlementDocument;
import com.chaing.domain.settlements.repository.interfaces.SettlementDocumentRepository;
import com.chaing.domain.settlements.service.SettlementDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementDocumentServiceImpl implements SettlementDocumentService {

    private final SettlementDocumentRepository documentRepository;

    @Override
    public SettlementDocument getDailyDocument(Long dailyReceiptId) {
        // 일별 문서는 1개라고 가정하고 단건 조회 (findByDailyReceiptId)
        return documentRepository.findByDailyReceiptId(dailyReceiptId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일별 영수증 문서를 찾을 수 없습니다."));
    }

    @Override
    public List<SettlementDocument> getMonthlyDocuments(Long monthlySettlementId) {
        // 월별 문서는 여러 개일 수 있으므로 목록 조회 (findAllByMonthlySettlementId)
        return documentRepository.findAllByMonthlySettlementId(monthlySettlementId);
    }
}
