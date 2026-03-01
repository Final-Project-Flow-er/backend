package com.chaing.api.controller.inbound;

import com.chaing.api.dto.inbound.request.InboundBoxSummaryRequest;
import com.chaing.api.dto.inbound.request.InboundConfirmRequest;
import com.chaing.api.dto.inbound.request.InboundDetailRequest;
import com.chaing.api.dto.inbound.request.InboundScanRequest;
import com.chaing.api.dto.inbound.response.InboundBoxSummaryResponse;
import com.chaing.api.dto.inbound.response.InboundDetailResponse;
import com.chaing.api.dto.inbound.response.InboundScanResponse;
import com.chaing.api.facade.inbound.InboundFacade;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // 입고 스캔
    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<InboundScanResponse>> scanInbound(
            @Valid @RequestBody InboundScanRequest request
    ) {
        // 공장/가맹점을 구분할 식별자가 필요 -> 로그인 권한 받아오기
        return ResponseEntity.ok(ApiResponse.success(InboundScanResponse.builder().build()));
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
