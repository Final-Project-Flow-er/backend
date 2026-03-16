package com.chaing.api.controller.franchise;

import com.chaing.api.facade.franchise.FranchiseReturnFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import com.chaing.core.dto.request.FranchiseReturnUpdateRequest;
import com.chaing.core.dto.returns.response.FranchiseReturnTargetResponse;
import com.chaing.domain.returns.dto.request.FranchiseReturnCreateRequest;
import com.chaing.domain.returns.dto.request.FranchiseReturnDeliveryRequest;
import com.chaing.domain.returns.dto.response.FranchiseReturnCreateResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnDeliveryResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnDetailResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnResponse;
import com.chaing.domain.returns.dto.response.FranchiseReturnUpdateResponse;
import com.chaing.domain.returns.dto.response.ReturnCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Operation(summary = "반품 조회", description = "가맹점 id로 해당 가맹점의 반품 요청 전체 조회 (페이지네이션)")
    @GetMapping
    @PreAuthorize("hasRole('FRANCHISE')")
    public ResponseEntity<ApiResponse<Page<FranchiseReturnResponse>>> getAllReturns(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.getAllReturnsPaged(userId, pageable)));
    }

    @Operation(summary = "특정 반품 조회", description = "가맹점 id와 반품 번호로 특정 반품 조회")
    @GetMapping("/{return-code}")
    @PreAuthorize("hasRole('FRANCHISE')")
    public ResponseEntity<ApiResponse<FranchiseReturnDetailResponse>> getReturn(
            @PathVariable("return-code") String returnCode,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.getReturn(userId, returnCode)));
    }

    @Operation(summary = "반품 수정", description = "가맹점 id와 반품 번호로 특정 반품 수정")
    @PatchMapping("/{return-code}")
    @PreAuthorize("hasRole('FRANCHISE')")
    public ResponseEntity<ApiResponse<FranchiseReturnUpdateResponse>> updateReturn(
            @PathVariable("return-code") String returnCode,
            @Valid @RequestBody List<FranchiseReturnUpdateRequest> requests,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.updateReturn(userId, requests, returnCode)));
    }

    @Operation(summary = "반품 취소", description = "가맹점 id와 반품 번호로 특정 반품 취소")
    @PatchMapping("/{return-code}/cancellation")
    @PreAuthorize("hasRole('FRANCHISE')")
    public ResponseEntity<ApiResponse<String>> cancelReturn(
            @PathVariable("return-code") String returnCode,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.cancel(userId, returnCode)));
    }

    @Operation(summary = "반품 대상 조회", description = "가맹점 id로 반품 생성의 대상이 되는 발주 조회")
    @GetMapping("/target")
    @PreAuthorize("hasRole('FRANCHISE')")
    public ResponseEntity<ApiResponse<List<FranchiseReturnTargetResponse>>> getTargets(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.getAllTargets(userId)));
    }

    @Operation(summary = "반품 생성 화면 데이터", description = "반품 요청 생성 시 발주 코드에 따라 보여지는 화면에 띄울 데이터 반환")
    @GetMapping("/{order-code}/target-info")
    @PreAuthorize("hasRole('FRANCHISE')")
    public ResponseEntity<ApiResponse<FranchiseReturnCreateResponse>> getReturnCreateInfo(
            @PathVariable("order-code") String orderCode,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.getReturnCreateInfo(userId, orderCode)));
    }

    @Operation(summary = "반품 생성", description = "가맹점 id로 반품 생성")
    @PostMapping
    @PreAuthorize("hasRole('FRANCHISE')")
    public ResponseEntity<ApiResponse<ReturnCreateResponse>> createReturn(
            @RequestBody FranchiseReturnCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getId();

        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.create(userId, request)));
    }

    @Operation(summary = "반품 출고", description = "외부 모듈용 가맹점에서 반품 신청한 제품들을 본사로 배송")
    @PatchMapping("/delivery")
    public ResponseEntity<ApiResponse<List<FranchiseReturnDeliveryResponse>>> returnDelivery(
            @Valid @RequestBody List<FranchiseReturnDeliveryRequest> requests
    ) {
        return ResponseEntity.ok(ApiResponse.success(franchiseReturnFacade.delivery(requests)));
    }
}
