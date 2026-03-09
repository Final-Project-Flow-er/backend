package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementConfirmMonthlyRequest;
import com.chaing.api.dto.hq.settlement.response.HQConfirmFranchiseResponse;
import com.chaing.api.dto.hq.settlement.response.HQConfirmStatusCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementConfirmFacade {

    // 1. 상단 상태별 카운트 조회
    public HQConfirmStatusCountResponse getMonthlyStatusCounts(YearMonth month) {
        return HQConfirmStatusCountResponse.of(0L, 0L, 0L); // 임시 0 반환
    }

    // 2. 월별 정산 확정 목록 페이징 조회
    public Page<HQConfirmFranchiseResponse> getMonthlyConfirmList(HQSettlementConfirmMonthlyRequest request) {
        return Page.empty();
    }

    // 3. 상태 변경: 작성중 -> 확정요청
    @Transactional
    public void requestConfirm(Long franchiseId, YearMonth month) {
        // TODO: DB Update 비즈니스 로직
    }

    // 4. 상태 변경: 확정요청 -> 최종확정 (단건)
    @Transactional
    public void finalizeConfirm(Long franchiseId, YearMonth month) {
        // TODO: DB Update 비즈니스 로직
    }

    // 5. 상태 롤백: 확정요청/최종확정 -> 작성중
    @Transactional
    public void rollbackToDraft(Long franchiseId, YearMonth month) {
        // TODO: DB Update 비즈니스 로직
    }

    // 6. 상태 변경: 일괄 최종확정
    @Transactional
    public void finalizeAll(YearMonth month) {
        // TODO: DB Update 비즈니스 로직
    }
}
