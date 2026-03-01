package com.chaing.api.facade.inbound;

import com.chaing.api.dto.inbound.request.InboundBoxSummaryRequest;
import com.chaing.api.dto.inbound.request.InboundDetailRequest;
import com.chaing.api.dto.inbound.response.InboundBoxSummaryResponse;
import com.chaing.api.dto.inbound.response.InboundDetailResponse;
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
public class InboundFacade {

    public List<InboundBoxSummaryResponse> getPendingBoxes(@Valid InboundBoxSummaryRequest request) {
        return null;
    }

    public List<InboundDetailResponse> getPendingItems(@Valid InboundDetailRequest request) {
        return null;
    }
}
