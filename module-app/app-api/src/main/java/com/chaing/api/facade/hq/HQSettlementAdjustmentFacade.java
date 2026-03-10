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

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementAdjustmentFacade {

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
        // TODO: AdjustmentService 등을 통해 DB에 Insert 로직
        throw new UnsupportedOperationException("조정 전표 등록 기능은 아직 구현되지 않았습니다.");
    }

    // 4. 조정 전표 목록 조회 (페이징)
    public Page<HQAdjustmentResponse> getAdjustments(HQSettlementAdjustmentListRequest request) {
        // TODO: AdjustmentService 등을 통해 실제 페이징 처리된 목록 반환
        return Page.empty();
    }
}
