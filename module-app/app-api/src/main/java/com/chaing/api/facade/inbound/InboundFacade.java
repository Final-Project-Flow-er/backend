package com.chaing.api.facade.inbound;

import com.chaing.api.dto.inbound.request.InboundBoxSummaryRequest;
import com.chaing.api.dto.inbound.request.InboundDetailRequest;
import com.chaing.api.dto.inbound.request.InboundScanBoxRequest;
import com.chaing.api.dto.inbound.request.InboundScanItemRequest;
import com.chaing.api.dto.inbound.response.InboundBoxSummaryResponse;
import com.chaing.api.dto.inbound.response.InboundDetailResponse;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.domain.inventories.dto.command.FactoryInboundCreateCommand;
import com.chaing.domain.inventories.dto.command.FranchiseInboundCreateCommand;
import com.chaing.domain.inventories.service.inbound.InboundService;
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

    private final InboundService<FranchiseInboundCreateCommand> franchiseInboundService;
    private final InboundService<FactoryInboundCreateCommand> factoryInboundService;

    public List<InboundBoxSummaryResponse> getPendingBoxes(
            @Valid InboundBoxSummaryRequest request) {
        return null;
    }

    public List<InboundDetailResponse> getPendingItems(
            @Valid InboundDetailRequest request) {
        return null;
    }

    public void scanInboundItem(@Valid InboundScanItemRequest request) {
        factoryInboundService.scanInbound(InboundScanItemRequest.from(request));
    }

    public void scanInboundBox(@Valid InboundScanBoxRequest request, UserPrincipal userPrincipal) {

        // franchiseID
        Long franchiseId = userPrincipal.getBusinessUnitId();
        franchiseInboundService.scanInbound(InboundScanBoxRequest.toCommand(request, franchiseId));
    }
}
