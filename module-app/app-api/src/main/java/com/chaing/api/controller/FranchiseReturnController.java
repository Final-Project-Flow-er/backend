package com.chaing.api.controller;

import com.chaing.api.dto.franchise.returns.request.FranchiseReturnCreateRequest;
import com.chaing.api.dto.franchise.returns.request.FranchiseReturnUpdateRequest;
import com.chaing.api.dto.franchise.returns.response.FranchiseReturnResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class FranchiseReturnController {

    @Operation(summary = "반품 조회", description = "가맹점 id로 해당 가맹점의 반품 요청 전체 조회")
    @GetMapping
    public ResponseEntity<List<FranchiseReturnResponse>> getAllReturns() {
        return ResponseEntity.ok(List.of(FranchiseReturnResponse.builder().build()));
    }

    @Operation(summary = "특정 반품 조회", description = "가맹점 id와 반품 번호로 특정 반품 조회")
    @GetMapping("/{return-id}")
    public ResponseEntity<FranchiseReturnResponse> getReturn(
            @PathVariable("return-id") String returnId
    ) {
        return ResponseEntity.ok(FranchiseReturnResponse.builder().build());
    }

    @Operation(summary = "반품 수정", description = "가맹점 id와 반품 번호로 특정 반품 수정")
    @PatchMapping("/{return-id}")
    public ResponseEntity<FranchiseReturnResponse> updateReturn(
            @PathVariable("return-id") String returnId,
            @RequestBody FranchiseReturnUpdateRequest request
    ) {
        return ResponseEntity.ok(FranchiseReturnResponse.builder().build());
    }

    @Operation(summary = "반품 취소", description = "가맹점 id와 반품 번호로 특정 반품 취소")
    @PatchMapping("/{return-id}/cancellation")
    public ResponseEntity<FranchiseReturnResponse> cancelReturn(
            @PathVariable("return-id") String returnId
    ) {
        return ResponseEntity.ok(FranchiseReturnResponse.builder().build());
    }

    @Operation(summary = "반품 생성", description = "가맹점 id로 반품 생성")
    @PostMapping
    public ResponseEntity<FranchiseReturnResponse> createReturn(
            @RequestBody FranchiseReturnCreateRequest request
    ) {
        return ResponseEntity.ok(FranchiseReturnResponse.builder().build());
    }
}
