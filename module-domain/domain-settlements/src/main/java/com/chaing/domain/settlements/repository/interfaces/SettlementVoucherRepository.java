package com.chaing.domain.settlements.repository.interfaces;

import com.chaing.domain.settlements.entity.SettlementVoucher;
import com.chaing.domain.settlements.enums.VoucherType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementVoucherRepository extends JpaRepository<SettlementVoucher, Long> {

    // 월별 정산의 전체 전표 (PDF/Excel용)
    List<SettlementVoucher> findAllByMonthlySettlementId(Long monthlySettlementId);

    // 월별 전표 페이징 (전표 상세 목록 — 전체 탭)
    Page<SettlementVoucher> findAllByMonthlySettlementId(
            Long monthlySettlementId, Pageable pageable);

    // 월별 전표 유형 필터 + 페이징 (전표 상세 목록 — 유형별 탭)
    Page<SettlementVoucher> findAllByMonthlySettlementIdAndVoucherType(
            Long monthlySettlementId, VoucherType voucherType, Pageable pageable);
}
