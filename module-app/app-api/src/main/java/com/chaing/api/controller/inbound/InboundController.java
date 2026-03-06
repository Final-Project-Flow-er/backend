package com.chaing.api.controller.inbound;

import com.chaing.api.dto.inbound.request.InboundBoxDetailRequest;
import com.chaing.api.dto.inbound.request.InboundConfirmRequest;
import com.chaing.api.dto.inbound.request.InboundScanBoxRequest;
import com.chaing.api.dto.inbound.request.InboundScanItemRequest;
import com.chaing.api.dto.inbound.response.InboundBoxSummaryResponse;
import com.chaing.api.dto.inbound.response.InboundDetailResponse;
import com.chaing.api.facade.inbound.InboundFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "공장 입고 스캔", description = "생산된 제품의 바코드를 스캔하여 정보를 확인합니다.")
    public ResponseEntity<ApiResponse<Void>> scanInboundItems(
            @Valid @RequestBody InboundScanItemRequest request
    ) {
        inboundFacade.scanInboundItem(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 입고 스캔(가맹)
    @PostMapping("/scan/box")
    @Operation(summary = "가맹점 입고 스캔", description = "입고된 박스의 바코드를 스캔하여 정보를 확인합니다.")
    public ResponseEntity<ApiResponse<Void>> scanInboundBoxes(
            @Valid @RequestBody InboundScanBoxRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        inboundFacade.scanInboundBox(request, userPrincipal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 가맹점 입고 대기 박스 목록 조회
    @GetMapping("/boxes")
    @Operation(summary = "입고 대기 박스 목록 조회", description = "현재 입고 대기 중인 박스들의 요약 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<InboundBoxSummaryResponse>>> getInboundBoxList(
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
        List<InboundBoxSummaryResponse> responses = inboundFacade.getPendingBoxes(userPrincipal.getBusinessId());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 가맹점 입고 대기 세부 목록 조회
    @GetMapping("/boxes/{boxCode}")
    @Operation(summary = "입고 대기 세부 목록 조회", description = "입고 대기 중인 박스나 상세 품목 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<InboundDetailResponse>>> getInboundItemDetails(
            @Valid InboundBoxDetailRequest request
    ) {
        List<InboundDetailResponse> responses = inboundFacade.getPendingBoxDetails(request.boxCode());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 공장 입고 대기 세부 목록 조회
    @GetMapping("/items")
    @Operation(summary = "입고 대기 세부 목록 조회", description = "현재 입고 대기 중인 상세 품목 리스트를 조회합니다.")
    public ResponseEntity<ApiResponse<List<InboundDetailResponse>>> getInboundItemDetails() {
        List<InboundDetailResponse> responses = inboundFacade.getPendingItems();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 입고 승인
    @PatchMapping("/confirm")
    @Operation(summary = "입고 승인", description = "해당 제품의 입고를 승인합니다.")
    public ResponseEntity<ApiResponse<Void>> updateInboundStatus(
            @Valid @RequestBody InboundConfirmRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
