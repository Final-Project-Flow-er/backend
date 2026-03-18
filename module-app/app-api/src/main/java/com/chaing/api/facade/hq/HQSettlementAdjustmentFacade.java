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
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementAdjustmentFacade {

    private final com.chaing.domain.settlements.service.SettlementAdjustmentService adjustmentService;
    private final com.chaing.domain.businessunits.repository.FranchiseRepository franchiseRepository;
    private final com.chaing.domain.users.repository.UserRepository userRepository;

    // 1. 드롭다운용 가맹점 목록 조회
    public List<HQAdjustmentFranchiseResponse> getFranchisesForDropdown() {
        return franchiseRepository.findAll().stream()
                .map(f -> HQAdjustmentFranchiseResponse.of(f.getFranchiseId(), f.getName()))
                .collect(Collectors.toList());
    }

    // 2. 드롭다운용 전표 유형 리스트 조회
    public List<VoucherType> getAdjustmentTypes() {
        return List.of(VoucherType.values()); // 실제 enum 값들 반환
    }

    // 3. 조정 전표 등록 (생성)
    @Transactional
    public void createAdjustment(HQSettlementAdjustmentVoucherRequest request, com.chaing.api.security.principal.UserPrincipal principal) {
        String createdByName = userRepository.findById(principal.getId())
                .map(com.chaing.domain.users.entity.User::getUsername)
                .orElse("Unknown");

        com.chaing.domain.settlements.enums.AdjustmentDirection direction = request.direction();
        BigDecimal amount = BigDecimal.valueOf(request.amount());
        BigDecimal finalAmount = (direction == com.chaing.domain.settlements.enums.AdjustmentDirection.INCREASE) 
                ? amount 
                : amount.negate();

        com.chaing.domain.settlements.entity.SettlementAdjustment adjustment = com.chaing.domain.settlements.entity.SettlementAdjustment
                .builder()
                .settlementVoucherId(0L) // 특정 전표 매핑이 없는 일반 조정의 경우 0L
                .adjustmentCode("AD-" + System.currentTimeMillis())
                .franchiseId(request.franchiseId())
                .voucherType(request.type())
                .occurredAt(request.occurredAt().atStartOfDay())
                .settlementMonth(request.settlementMonth().toString()) 
                .isMinus(direction == com.chaing.domain.settlements.enums.AdjustmentDirection.DECREASE)
                .createdByName(createdByName)
                .adjustmentAmount(finalAmount)
                .reason(request.reason())
                .createdBy(principal.getId())
                .build();

        adjustmentService.create(adjustment);
    }

    // 4. 조정 전표 목록 조회 (페이징)
    public Page<HQAdjustmentResponse> getAdjustments(HQSettlementAdjustmentListRequest request) {
        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 20;
        Pageable pageable = PageRequest.of(page, size);

        String settlementMonth = request.month() != null ? request.month().toString() : null;

        Page<com.chaing.domain.settlements.entity.SettlementAdjustment> adjustments = adjustmentService.getAll(
                request.franchiseId(), request.type(), settlementMonth, pageable);

        List<Long> franchiseIds = adjustments.stream()
                .map(com.chaing.domain.settlements.entity.SettlementAdjustment::getFranchiseId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> franchiseNameMap = franchiseRepository.findNamesByIds(franchiseIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));

        List<HQAdjustmentResponse> dtos = adjustments.stream()
                .map(a -> HQAdjustmentResponse.of(
                        a.getSettlementAdjustmentId(),
                        franchiseNameMap.getOrDefault(a.getFranchiseId(), "Unknown"),
                        a.getVoucherType(),
                        a.getOccurredAt().toLocalDate(),
                        a.getAdjustmentAmount().longValue(),
                        a.getIsMinus() ? com.chaing.domain.settlements.enums.AdjustmentDirection.DECREASE : com.chaing.domain.settlements.enums.AdjustmentDirection.INCREASE,
                        a.getReason(),
                        a.getSettlementMonth())) // ⭐️ 응답에 반영월 포함
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, adjustments.getTotalElements());
    }
}

