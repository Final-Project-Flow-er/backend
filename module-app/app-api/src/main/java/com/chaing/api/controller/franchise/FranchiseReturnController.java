package com.chaing.api.controller.franchise;

import com.chaing.api.dto.franchise.returns.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnUpdateRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnAndReturnItemResponse;
import com.chaing.api.facade.franchise.FranchiseReturnFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnDetailResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Franchise Return API", description = "가맹점 반품 관련 API")
@RequestMapping("/api/v1/franchise/returns")
@RequiredArgsConstructor
public class FranchiseReturnController {

    private final FranchiseReturnFacade franchiseReturnFacade;

    @Operation(summary = "반품 조회", description = "가맹점 id로 해당 가맹점의 반품 요청 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FranchiseReturnResponse>>> getAllReturns() {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.getAllReturns(username)));
    }

    @Operation(summary = "특정 반품 조회", description = "가맹점 id와 반품 번호로 특정 반품 조회")
    @GetMapping("/{return-code}")
    public ResponseEntity<ApiResponse<FranchiseReturnDetailResponse>> getReturn(
            @PathVariable("return-code") String returnCode
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.getReturn(username, returnCode)));
    }

    @Operation(summary = "반품 수정", description = "가맹점 id와 반품 번호로 특정 반품 수정")
    @PatchMapping("/{return-code}")
    public ResponseEntity<ApiResponse<FranchiseReturnUpdateResponse>> updateReturn(
            @PathVariable("return-code") String returnCode,
            @RequestBody List<FranchiseReturnUpdateRequest> requests
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.updateReturn(username, requests, returnCode)));
    }

    @Operation(summary = "반품 취소", description = "가맹점 id와 반품 번호로 특정 반품 취소")
    @PatchMapping("/{return-number}/cancellation")
    public ResponseEntity<ApiResponse<FranchiseReturnAndReturnItemResponse>> cancelReturn(
            @PathVariable("return-number") String returnNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseReturnAndReturnItemResponse.builder().build()));
    }

    @Operation(summary = "반품 생성", description = "가맹점 id로 반품 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<FranchiseReturnAndReturnItemResponse>> createReturn(
            @RequestBody FranchiseReturnCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseReturnAndReturnItemResponse.builder().build()));
    }
}
