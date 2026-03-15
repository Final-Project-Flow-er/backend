package com.chaing.api.controller.hq;

import com.chaing.api.dto.hq.businessunit.request.BusinessUnitCreateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitStatusUpdateRequest;
import com.chaing.api.dto.hq.businessunit.request.BusinessUnitUpdateRequest;
import com.chaing.api.dto.hq.businessunit.response.BusinessUnitDetailResponse;
import com.chaing.api.dto.hq.businessunit.response.BusinessUnitSummaryResponse;
import com.chaing.api.facade.hq.BusinessUnitManagementFacade;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.businessunits.enums.BusinessUnitType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('HQ')")
@Tag(name = "Business Unit Management API", description = "사업장 통합 관리 API")
@RequestMapping("/api/v1/hq/business-units")
public class BusinessUnitManagementController {

    private final BusinessUnitManagementFacade businessUnitManagementFacade;

    @Operation(summary = "신규 사업장 등록", description = "본사 관리자가 가맹점 또는 공장을 등록")
    @PostMapping("/{type}")
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> createBusinessUnit(
            @Schema(allowableValues = {"franchise", "factory"})
            @PathVariable BusinessUnitType type,
            @Valid @RequestBody BusinessUnitCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(businessUnitManagementFacade.createBusinessUnit(type, request)));
    }

    @Operation(summary = "사업장 목록 조회", description = "사업장 목록을 페이징하여 조회")
    @GetMapping("/{type}")
    public ResponseEntity<ApiResponse<Page<BusinessUnitSummaryResponse>>> getBusinessUnits(
            @Schema(allowableValues = {"franchise", "factory"})
            @PathVariable BusinessUnitType type,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(businessUnitManagementFacade.getList(type, pageable)));
    }

    @Operation(summary = "사업장 상세 조회", description = "특정 사업장의 상세 정보 조회")
    @GetMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> getBusinessUnitDetail(
            @Schema(allowableValues = {"hq", "franchise", "factory"})
            @PathVariable BusinessUnitType type,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(businessUnitManagementFacade.getDetail(type, id)));
    }

    @Operation(summary = "사업장 정보 수정", description = "특정 사업장의 정보 수정")
    @PatchMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> updateBusinessUnit(
            @Schema(allowableValues = {"hq", "franchise", "factory"})
            @PathVariable BusinessUnitType type,
            @PathVariable Long id,
            @Valid @RequestBody BusinessUnitUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(businessUnitManagementFacade.updateInfo(type, id, request)));
    }

    @Operation(summary = "사업장 상태 변경", description = "사업장의 상태를 활성화 또는 비활성화 상태로 변경")
    @PatchMapping("/{type}/{id}/status")
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> updateBusinessUnitStatus(
            @Schema(allowableValues = {"franchise", "factory"})
            @PathVariable BusinessUnitType type,
            @PathVariable Long id,
            @Valid @RequestBody BusinessUnitStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(businessUnitManagementFacade.updateStatus(type, id, request)));
    }

    @Operation(summary = "사업장 삭제", description = "사업장 삭제 (soft delete)")
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBusinessUnit(
            @Schema(allowableValues = {"franchise", "factory"})
            @PathVariable BusinessUnitType type,
            @PathVariable Long id
    ) {
        businessUnitManagementFacade.deleteBusinessUnit(type, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "가맹점 사진 관리", description = "사진 등록, 일부 삭제, 신규 추가를 통합 처리")
    @PostMapping(value = "/franchise/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> manageFranchiseImages(
            @PathVariable Long id,
            @RequestParam(value = "deleteStoredFileNames", required = false) List<String> deleteStoredFileNames,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                businessUnitManagementFacade.updateFranchiseImages(id, deleteStoredFileNames, images)
        ));
    }

    @Operation(summary = "가맹점 경고 부여", description = "가맹점에 경고 부여 (경고 3회 시 한 달간 반품 정지)")
    @PostMapping("/franchise/{id}/warnings")
    public ResponseEntity<ApiResponse<BusinessUnitDetailResponse>> addWarning(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(businessUnitManagementFacade.addWarning(BusinessUnitType.FRANCHISE, id)));
    }
}
