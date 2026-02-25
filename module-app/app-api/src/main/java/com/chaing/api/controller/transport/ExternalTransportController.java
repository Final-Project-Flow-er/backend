package com.chaing.api.controller.transport;

import com.chaing.api.dto.transport.external.request.ExternalTransportRegisterRequest;
import com.chaing.api.dto.transport.external.response.TrackingNumberResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "External Transport API", description = "외부 운송(택배) 관련 API")
@RequestMapping("/api/v1/transport/external")
public class ExternalTransportController {

    @Operation(summary = "운송장 생성", description = "운송장 번호 생성")
    @PostMapping("/tracking-number")
    public ApiResponse<TrackingNumberResponse> createTrackingNumber(
            @RequestBody ExternalTransportRegisterRequest registerDto) {

        // TODO: 운송장 번호 생성 및 DB 저장
        // 예: String randomTrackingNum = UUID.randomUUID().toString();

        return ApiResponse.success(new TrackingNumberResponse("WAYBILL-12345-6789"));
    }

    @Operation(summary = "배송 완료 처리", description = "배송 완료 상태 변경")
    @PatchMapping("/{transportId}/complete")
    public ApiResponse<String> completeDelivery(@PathVariable Long transportId) {
        return ApiResponse.success("배송 완료");
    }
}