package com.chaing.api.facade.outbound;

import com.chaing.api.dto.outbound.request.OutboundUpdateRequest;
import com.chaing.core.enums.LogType;
import com.chaing.domain.inventories.service.OutboundService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Getter
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Validated
public class OutboundFacade {

    private final OutboundService outboundService;

    // 재고 상태 변경
    public void updateOutboundStatus(@Valid OutboundUpdateRequest request, LogType currentStatus) {
        List<String> selectedList = request.serialCodes();
        outboundService.updateStatus(selectedList, currentStatus);
    }

    // 박스 할당
    public void assignBoxToInventories(String boxCode, List<String> serialCodes) {
        outboundService.assignBox(boxCode, serialCodes);
    }
}
