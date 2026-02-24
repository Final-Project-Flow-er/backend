package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.management.request.UpdateHQRequest;
import com.chaing.api.dto.hq.management.response.HQInfoResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "HQ Management API", description = "본사 관리 API")
@RequestMapping("/api/v1/hq/management")
public class HQManagementController {

    @Operation(summary = "본사 정보 조회", description = "본사의 상세 정보 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<HQInfoResponse>> getHqInfo() {
        return ResponseEntity.ok(ApiResponse.success(HQInfoResponse.builder().build()));
    }

    @Operation(summary = "본사 정보 수정", description = "본사 기본 정보 수정")
    @PatchMapping
    public ResponseEntity<ApiResponse<HQInfoResponse>> updateHqInfo(
            @Valid @RequestBody UpdateHQRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQInfoResponse.builder().build()));
    }
}
