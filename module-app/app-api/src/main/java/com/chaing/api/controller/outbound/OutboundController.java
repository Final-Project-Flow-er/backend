package com.chaing.api.controller.outbound;

import com.chaing.api.dto.outbound.request.OutboundAssignRequest;
import com.chaing.api.dto.outbound.request.OutboundBoxSummaryRequest;
import com.chaing.api.dto.outbound.request.OutboundCancelRequest;
import com.chaing.api.dto.outbound.request.OutboundConfirmRequest;
import com.chaing.api.dto.outbound.request.OutboundItemRequest;
import com.chaing.api.dto.outbound.request.OutboundPickingRequest;
import com.chaing.api.dto.outbound.request.OutboundReadyRequest;
import com.chaing.api.dto.outbound.request.OutboundUpdateRequest;
import com.chaing.api.dto.outbound.response.OutboundBoxSummaryResponse;
import com.chaing.api.dto.outbound.response.OutboundItemResponse;
import com.chaing.api.facade.outbound.OutboundFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.core.enums.LogType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name="Outbound API", description = "출고 관련 API")
@RequestMapping("/api/v1/outbounds")
public class OutboundController {

    private final OutboundFacade outboundFacade;

    // 출고 제품 스캔 및 피킹 대기 상태 변경
    @PatchMapping("/scan")
    @Operation(summary = "출고 스캔", description = "출고를 위해 제품을 스캔합니다.")
    public ResponseEntity<ApiResponse<Void>> scanOutbound(
        @Valid @RequestBody OutboundUpdateRequest request
    ) {
        LogType currentStatus = LogType.AVAILABLE;
        outboundFacade.updateOutboundStatus(request, currentStatus);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 박스 할당
    @PatchMapping("/assign")
    @Operation(summary = "박스 할당", description = "스캔된 제품에 박스를 할당합니다.")
    public ResponseEntity<ApiResponse<Void>> assignBox(
            @Valid @RequestBody OutboundAssignRequest request
    ) {
        outboundFacade.assignBoxToInventories(request.boxCode(), request.serialCodes());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 피킹 확정
    @PatchMapping("/picking")
    @Operation(summary = "피킹 확정", description = "피킹 대기 상태의 제품의 피킹을 확정합니다.")
    public ResponseEntity<ApiResponse<Void>> pickOutbound(
        @Valid @RequestBody OutboundUpdateRequest request
    ){
        LogType currentStatus = LogType.PICKING_WAIT;
        outboundFacade.updateOutboundStatus(request, currentStatus);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 출고 확정
    @PatchMapping("/confirm")
    @Operation(summary = "출고 확정", description = "피킹 상태 제품의 출고를 확정합니다.")
    public ResponseEntity<ApiResponse<Void>> confirmOutbound(
            @Valid @RequestBody OutboundUpdateRequest request
    ) {
        LogType currentStatus = LogType.PICKING;
        outboundFacade.updateOutboundStatus(request, currentStatus);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 할당 취소 및 상태 변경
    @PatchMapping("/cancel")
    @Operation(summary = "할당 취소 및 상태 변경", description = "제품에 할당된 박스 코드를 취소하고 가용 상태로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> cancelOutbound(
            @Valid @RequestBody OutboundCancelRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 박스 목록 조회
    @GetMapping("/boxes")
    @Operation(summary = "출고 스캔 박스 목록 조회", description = "스캔된 박스 단위 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<OutboundBoxSummaryResponse>>> getBoxDetail(
            @Valid OutboundBoxSummaryRequest request
    ) {
        List<OutboundBoxSummaryResponse> responses = List.of();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // 상세 목록 조회
    @GetMapping("/boxes/items")
    @Operation(summary = "출고 상세 목록 조회", description = "스캔된 제품의 상세 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<OutboundItemResponse>>> getItemDetail(
            @Valid OutboundItemRequest request
    ) {
        List<OutboundItemResponse> responses = List.of();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}