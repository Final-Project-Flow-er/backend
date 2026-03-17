package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.DailyReceiptLine;
import com.chaing.domain.settlements.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyReceiptLineRepository extends JpaRepository<DailyReceiptLine, Long> {

        // 특정 일별 영수증 목록
        List<DailyReceiptLine> findAllByDailyReceiptId(Long dailyReceiptId);

        // 특정 일별 영수증 (유형별 필터 + 페이징) — 전표 상세 목록 페이지
        Page<DailyReceiptLine> findAllByDailyReceiptId(Long dailyReceiptId, Pageable pageable);

        // 유형 필터링 + 페이징
        Page<DailyReceiptLine> findAllByDailyReceiptIdAndLineType(
                        Long dailyReceiptId, VoucherType lineType, Pageable pageable);
}
