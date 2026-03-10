package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementConfirmMonthlyRequest;
import com.chaing.api.dto.hq.settlement.response.HQConfirmFranchiseResponse;
import com.chaing.api.dto.hq.settlement.response.HQConfirmStatusCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementConfirmFacade {

    private final com.chaing.domain.settlements.service.MonthlySettlementService monthlyService;

    // 1. 상단 상태별 카운트 조회
    public HQConfirmStatusCountResponse getMonthlyStatusCounts(YearMonth month) {
        Map<String, Long> counts = monthlyService.getStatusCounts(month);
        return HQConfirmStatusCountResponse.of(
                counts.getOrDefault("CALCULATED", 0L),
                counts.getOrDefault("CONFIRM_REQUESTED", 0L),
                counts.getOrDefault("CONFIRMED", 0L));
    }

    // 2. 월별 정산 확정 목록 페이징 조회
    public Page<HQConfirmFranchiseResponse> getMonthlyConfirmList(HQSettlementConfirmMonthlyRequest request) {
        List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService
                .getAllByMonth(request.month(), request.keyword());

        if (request.status() != null) {
            settlements = settlements.stream()
                    .filter(s -> s.getStatus() == request.status())
                    .collect(Collectors.toList());
        }

        List<HQConfirmFranchiseResponse> dtos = settlements.stream()
                .map(s -> HQConfirmFranchiseResponse.of(
                        s.getFranchiseId(),
                        "가맹점명 (추후 연동)", // TODO: Franchise API 연동
                        s.getFinalSettlementAmount().longValue(),
                        s.getStatus()))
                .collect(Collectors.toList());

        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 20;
        Pageable pageable = PageRequest.of(page, size);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());

        if (start > dtos.size()) {
            return new PageImpl<>(List.of(), pageable, dtos.size());
        }

        return new PageImpl<>(dtos.subList(start, end), pageable, dtos.size());
    }

    // 3. 상태 변경: 작성중 -> 확정요청
    @Transactional
    public void requestConfirm(Long franchiseId, YearMonth month) {
        com.chaing.domain.settlements.entity.MonthlySettlement ms = monthlyService.getByFranchiseAndMonth(franchiseId,
                month);
        monthlyService.requestConfirm(ms.getMonthlySettlementId());
    }

    // 4. 상태 변경: 확정요청 -> 최종확정 (단건)
    @Transactional
    public void finalizeConfirm(Long franchiseId, YearMonth month) {
        com.chaing.domain.settlements.entity.MonthlySettlement ms = monthlyService.getByFranchiseAndMonth(franchiseId,
                month);
        monthlyService.confirm(ms.getMonthlySettlementId());
    }

    // 5. 상태 롤백: 확정요청/최종확정 -> 작성중
    @Transactional
    public void rollbackToDraft(Long franchiseId, YearMonth month) {
        com.chaing.domain.settlements.entity.MonthlySettlement ms = monthlyService.getByFranchiseAndMonth(franchiseId,
                month);
        monthlyService.rollback(ms.getMonthlySettlementId());
    }

    // 6. 상태 변경: 일괄 최종확정
    @Transactional
    public void finalizeAll(YearMonth month) {
        List<com.chaing.domain.settlements.entity.MonthlySettlement> settlements = monthlyService.getAllByMonth(month,
                null);
        settlements.stream()
                .filter(s -> s.getStatus() == com.chaing.domain.settlements.enums.SettlementStatus.CONFIRM_REQUESTED)
                .forEach(s -> monthlyService.confirm(s.getMonthlySettlementId()));
    }
}
