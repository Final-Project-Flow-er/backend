package com.chaing.api.controller.franchise;

import com.chaing.domain.sales.dto.request.FranchiseSellRequest;
import com.chaing.api.dto.franchise.sales.response.FranchiseSalesResponse;
import com.chaing.api.facade.franchise.FranchiseSalesFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.sales.dto.response.FranchiseSalesDetailResponse;
import com.chaing.domain.sales.dto.response.FranchiseSellResponse;
import com.chaing.domain.sales.service.FranchiseSalesService;
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
@Tag(name = "Franchise Sales API", description = "가맹점 판매 관련 API")
@RequestMapping("/api/v1/franchise/sales")
@RequiredArgsConstructor
public class FranchiseSalesController {

    private final FranchiseSalesFacade franchiseSalesFacade;
    private final FranchiseSalesService franchiseSalesService;

    @Operation(summary = "판매 조회", description = "가맹점 id로 해당 가맹점의 판매 전체 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FranchiseSalesResponse>>> getAllSales() {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(franchiseSalesFacade.getAllSales(username)));
    }

    @Operation(summary = "판매 세부정보 조회", description = "가맹점 id와 판매 코드로 판매 세부사항 조회")
    @GetMapping("/{sales-code}")
    public ResponseEntity<ApiResponse<FranchiseSalesDetailResponse>> getSalesDetail(
            @PathVariable("sales-code") String salesCode
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        return ResponseEntity.ok(ApiResponse.success(franchiseSalesFacade.getSalesDetail(username, salesCode)));
    }

    @Operation(summary = "판매 취소", description = "가맹점 id와 판매 코드로 특정 판매 취소")
    @PatchMapping("/{sales-number}")
    public ResponseEntity<ApiResponse<FranchiseSalesResponse>> cancelSales(
            @PathVariable("sales-number") String salesNumber
    ) {
        return ResponseEntity.ok(ApiResponse.success(FranchiseSalesResponse.builder().build()));
    }

    @Operation(summary = "판매 생성", description = "가맹점 id로 판매 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<FranchiseSellResponse>> createSale(
            @RequestBody FranchiseSellRequest request
    ) {
        //TODO: Spring Security Context에서 값 꺼내오는 걸로 수정해야 함
        String username = "test";

        //TODO: request의 product 검증 -> 해야하나? 잘 모르겠네 아직은

        return ResponseEntity.ok(ApiResponse.success(franchiseSalesFacade.sell(username, request)));
    }
}
