package com.chaing.domain.settlements.service;

import com.chaing.domain.settlements.entity.MonthlySettlement;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface MonthlySettlementService {

    // 특정 월 전체 가맹점 (keyword -> 가맹점)
    List<MonthlySettlement> getAllByMonth(YearMonth month, String keyword);

    // 특정 가맹점 + 월
    MonthlySettlement getByFranchiseAndMonth(Long franchiseId, YearMonth month);

    // 정산 확정 - 상단 숫자 카드
    Map<String, Long> getStatusCounts(YearMonth month);

    // 확정 요청 - 상단 숫자 카드
    MonthlySettlement requestConfirm(Long monthlySettlementId);

    // 최종 확정 - 상단 숫자 카드
    MonthlySettlement confirm(Long monthlySettlementId);

    // 확정 취소 (수정 버튼)
    MonthlySettlement rollback(Long monthlySettlementId);
}
