package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementAdjustmentListRequest;
import com.chaing.api.dto.hq.settlement.request.HQSettlementAdjustmentVoucherRequest;
import com.chaing.api.dto.hq.settlement.response.HQAdjustmentFranchiseResponse;
import com.chaing.api.dto.hq.settlement.response.HQAdjustmentResponse;
import com.chaing.domain.settlements.enums.VoucherType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementAdjustmentFacade {

    private final com.chaing.domain.settlements.service.SettlementAdjustmentService adjustmentService;

    // 1. 드롭다운용 가맹점 목록 조회
    public List<HQAdjustmentFranchiseResponse> getFranchisesForDropdown() {
        // TODO: FranchiseService 등을 통해 실제 목룍 조회
        return List.of();
    }

    // 2. 드롭다운용 전표 유형 리스트 조회
    public List<VoucherType> getAdjustmentTypes() {
        return List.of(VoucherType.values()); // 실제 enum 값들 반환
    }

    // 3. 조정 전표 등록 (생성)
    @Transactional
    public void createAdjustment(HQSettlementAdjustmentVoucherRequest request) {
        com.chaing.domain.settlements.entity.SettlementAdjustment adjustment = com.chaing.domain.settlements.entity.SettlementAdjustment
                .builder()
                .settlementVoucherId(0L) // TODO: 전표 ID 매핑 또는 참조 로직 추가
                .adjustmentCode("AD-" + System.currentTimeMillis()) // 임시 코드 채번
                .franchiseId(request.franchiseId())
                .voucherType(request.type())
                .occurredAt(request.occurredAt().atStartOfDay())
                .isMinus(request.isMinus())
                .createdByName("System") // TODO: Security/Auth에서 작성자 주입
                .adjustmentAmount(BigDecimal.valueOf(request.amount()))
                .reason(request.reason())
                .createdBy(0L) // TODO: 실제 작성자 ID 주입
                .build();

        adjustmentService.create(adjustment);
    }

    // 4. 조정 전표 목록 조회 (페이징)
    public Page<HQAdjustmentResponse> getAdjustments(HQSettlementAdjustmentListRequest request) {
        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 20;
        Pageable pageable = PageRequest.of(page, size);

        Page<com.chaing.domain.settlements.entity.SettlementAdjustment> adjustments = adjustmentService.getAll(
                request.franchiseId(), request.type(), pageable);

        // (주의: request.month() 조건은 SettlementAdjustment에 현재 필드가 없으므로, 향후 월별 필터 로직 추가
        // 필요)

        List<HQAdjustmentResponse> dtos = adjustments.stream()
                .map(a -> HQAdjustmentResponse.of(
                        a.getSettlementAdjustmentId(),
                        "가맹점명 (추후 연동)", // TODO: Franchise API 연동
                        a.getVoucherType(),
                        a.getOccurredAt().toLocalDate(),
                        a.getAdjustmentAmount().longValue(),
                        a.getIsMinus(),
                        a.getReason()))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, adjustments.getTotalElements());
    }
}
