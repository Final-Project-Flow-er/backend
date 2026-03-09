package com.chaing.api.facade.hq;

import com.chaing.api.dto.hq.settlement.request.HQSettlementLogRequest;
import com.chaing.api.dto.hq.settlement.response.HQSettlementLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HQSettlementLogFacade {

    // 1. 본사 정산 로그(이력) 리스트 페이징 조회
    public Page<HQSettlementLogResponse> getSettlementLogs(HQSettlementLogRequest request) {
        // TODO: LogService를 통해 실제 페이징 데이터 및 로그 데이터 조회
        return Page.empty();
    }

}
