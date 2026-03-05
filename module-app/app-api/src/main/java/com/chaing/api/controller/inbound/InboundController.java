package com.chaing.api.controller.inbound;

import com.chaing.api.dto.inbound.request.InboundBoxSummaryRequest;
import com.chaing.api.dto.inbound.request.InboundConfirmRequest;
import com.chaing.api.dto.inbound.request.InboundDetailRequest;
import com.chaing.api.dto.inbound.request.InboundScanBoxRequest;
import com.chaing.api.dto.inbound.request.InboundScanItemRequest;
import com.chaing.api.dto.inbound.response.InboundBoxSummaryResponse;
import com.chaing.api.dto.inbound.response.InboundDetailResponse;
import com.chaing.api.facade.inbound.InboundFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.businessunits.entity.Franchise;
import com.chaing.domain.users.entity.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Inbound API", description = "입고 API")
@RequestMapping("/api/v1/inbounds")
@RequiredArgsConstructor
public class InboundController {

    private final InboundFacade inboundFacade;

    // 입고 스캔(공장)
    @PostMapping("/scan/item")
    public ResponseEntity<ApiResponse<Void>> scanInboundItems(
            @Valid @RequestBody InboundScanItemRequest request
    ) {
        inboundFacade.scanInboundItem(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 입고 스캔(가맹)
    @PostMapping("/scan/box")
    public ResponseEntity<ApiResponse<Void>> scanInboundBoxes(
            @Valid @RequestBody InboundScanBoxRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        inboundFacade.scanInboundBox(request, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 입고 대기 박스 목록 조회
    @GetMapping("/boxes")
    public ResponseEntity<ApiResponse<List<InboundBoxSummaryResponse>>> getInboundBoxList(
            @Valid InboundBoxSummaryRequest request
    ) {
        List<InboundBoxSummaryResponse> responses = inboundFacade.getPendingBoxes(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 입고 대기 세부 목록 조회
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<InboundDetailResponse>>> getInboundItemDetails(
            @Valid InboundDetailRequest request
    ) {
        List<InboundDetailResponse> responses = inboundFacade.getPendingItems(request);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 입고 승인
    @PatchMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> updateInboundStatus(
            @Valid @RequestBody InboundConfirmRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
