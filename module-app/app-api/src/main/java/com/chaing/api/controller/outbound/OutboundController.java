package com.chaing.api.controller.outbound;

import com.chaing.api.dto.outbound.request.OutboundAssignRequest;
import com.chaing.api.dto.outbound.request.OutboundBoxSummaryRequest;
import com.chaing.api.dto.outbound.request.OutboundCancelRequest;
import com.chaing.api.dto.outbound.request.OutboundConfirmRequest;
import com.chaing.api.dto.outbound.request.OutboundItemRequest;
import com.chaing.api.dto.outbound.request.OutboundPickingRequest;
import com.chaing.api.dto.outbound.request.OutboundReadyRequest;
import com.chaing.api.dto.outbound.request.OutboundScanRequest;
import com.chaing.api.dto.outbound.response.OutboundBoxSummaryResponse;
import com.chaing.api.dto.outbound.response.OutboundItemResponse;
import com.chaing.api.dto.outbound.response.OutboundScanResponse;
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
@RequiredArgsConstructor
@Tag(name="Outbound API", description = "출고 관련 API")
@RequestMapping("/api/v1/outbound")
public class OutboundController {

    // 출고 제품 스캔
    @PostMapping
    public ResponseEntity<ApiResponse<OutboundScanResponse>> scanOutbound(
        @Valid @RequestBody OutboundScanRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(OutboundScanResponse.builder().build()));
    }

    // 할당 및 피킹 대기 상태 변경
    @PatchMapping("/assign")
    public ResponseEntity<ApiResponse<Void>> assignInventoryToBox(
            @Valid @RequestBody OutboundAssignRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 피킹 확정
    @PatchMapping("/picking")
    public ResponseEntity<ApiResponse<Void>> pickOutbound(
        @Valid @RequestBody OutboundPickingRequest request
    ){
        return ResponseEntity.ok(ApiResponse.success(null));
    }


    // 출고 대기 상태 변경
    @PatchMapping("/ready")
    public ResponseEntity<ApiResponse<Void>> readyOutbound(
            @Valid @RequestBody OutboundReadyRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 출고 확정
    @PatchMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmOutbound(
            @Valid @RequestBody OutboundConfirmRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 할당 취소 및 상태 변경
    @PatchMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOutbound(
            @Valid @RequestBody OutboundCancelRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 박스 목록 조회
    @GetMapping("/boxes")
    public ResponseEntity<ApiResponse<List<OutboundBoxSummaryResponse>>> getBoxDetail(
            @Valid OutboundBoxSummaryRequest request
    ) {
        List<OutboundBoxSummaryResponse> responses = List.of();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 상세 목록 조회
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<OutboundItemResponse>>> getItemDetail(
            @Valid OutboundItemRequest request
    ) {
        List<OutboundItemResponse> responses = List.of();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
