package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.entity.DailySettlementReceipt;
import com.chaing.domain.settlements.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailySettlementService {
    // 특정 날짜 전체 가맹점 일별 정산 목록 (keyword -> 가맹점)
    List<DailySettlementReceipt> getAllByDate(LocalDate date, String keyword);

    // 특정 가맹점 + 날짜
    DailySettlementReceipt getByFranchiseAndDate(Long franchiseId, LocalDate date);

    // 특정 가맹점 + 날짜 (Optional 반환, 트랜잭션 rollback-only 방지용)
    Optional<DailySettlementReceipt> findByFranchiseAndDate(Long franchiseId, LocalDate date);

    // 기간별 조회 (그래프용)
    List<DailySettlementReceipt> getAllByDateRange(LocalDate start, LocalDate end);

    // 특정 가맹점의 기간별 조회 (가맹점 매출 추이 그래프)
    List<DailySettlementReceipt> getAllByFranchiseAndDateRange(
            Long franchiseId, LocalDate start, LocalDate end);

    // 일별 전표 목록 (유형 필터 + 페이징)
    Page<DailyReceiptLine> getReceiptLines(Long dailyReceiptId, VoucherType type, Pageable pageable);

    // 일별 전표 전체 목록 (PDF/Excel용)
    List<DailyReceiptLine> getAllReceiptLines(Long dailyReceiptId);

    // 정산 데이터 저장
    DailySettlementReceipt save(DailySettlementReceipt receipt);
}
