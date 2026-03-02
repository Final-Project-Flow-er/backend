package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.returns.request.HQReturnProductRequest;
import com.chaing.api.dto.hq.returns.request.HQReturnUpdateRequest;
import com.chaing.api.dto.hq.response.HQReturnProductResponse;
import com.chaing.api.dto.hq.response.HQReturnResponse;
import com.chaing.api.facade.hq.HQReturnFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "HQ Return API", description = "본사 반품 관련 API")
@RequestMapping("/api/v1/hq/returns")
@RequiredArgsConstructor
public class HQReturnController {

    private final HQReturnFacade hqReturnFacade;

    @Operation(summary = "반품 요청 조회", description = "반품 요청 전체 조회")
    @GetMapping
    @PreAuthorize("hasRole('HQ')")
    public ResponseEntity<ApiResponse<List<HQReturnResponse>>> getAllReturns(
            @RequestParam Boolean isAccepted,
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
        String username = userPrincipal.getUsername();

        return ResponseEntity.ok(ApiResponse.success(hqReturnFacade.getAllReturns(username, isAccepted)));
    }

    @Operation(summary = "특정 반품 요청 조회", description = "특정 반품 요청 조회")
    @GetMapping("/{return-number}")
    public ResponseEntity<ApiResponse<HQReturnResponse>> getReturn(
            @PathVariable("return-number") String returnNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQReturnResponse.builder().build()));
    }

    @Operation(summary = "반품 요청 상태 변경", description = "접수, 검수 등으로 상태 변경")
    @PatchMapping("/{return-number}")
    public ResponseEntity<ApiResponse<HQReturnResponse>> updateReturn(
            @PathVariable("return-number") String returnNumber,
            @RequestBody HQReturnUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(HQReturnResponse.builder().build()));
    }

    @Operation(summary = "반품 제품 검수", description = "반품 요청 들어온 상품 검수")
    @PatchMapping("/{return-number}/inspection")
    public ResponseEntity<ApiResponse<List<HQReturnProductResponse>>> inspectProduct(
            @PathVariable("return-number") String returnNumber,
            @RequestBody HQReturnProductRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(List.of(HQReturnProductResponse.builder().build())));
    }
}
