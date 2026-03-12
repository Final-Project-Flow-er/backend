package com.chaing.api.controller.notice;

import com.chaing.api.dto.notice.request.CreateNoticeRequest;
import com.chaing.api.dto.notice.request.UpdateNoticeRequest;
import com.chaing.api.dto.notice.response.NoticeDetailResponse;
import com.chaing.api.dto.notice.response.NoticeSummaryResponse;
import com.chaing.api.facade.notice.NoticeFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notice API", description = "공지사항 API")
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeFacade noticeFacade;

    @PreAuthorize("hasRole('HQ')")
    @Operation(summary = "공지사항 등록", description = "본사 관리자가 새로운 공지사항 등록")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> createNotice(
            @RequestPart("request") @Valid CreateNoticeRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(noticeFacade.createNotice(request, images, principal.getId())));
    }

    @Operation(summary = "공지사항 목록 조회", description = "전체 공지사항 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeSummaryResponse>>> getNotices(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(noticeFacade.getNoticeList(pageable)));
    }

    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 내용 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> getNoticeDetail(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(noticeFacade.getNoticeDetail(id)));
    }

    @PreAuthorize("hasRole('HQ')")
    @Operation(summary = "공지사항 수정", description = "특정 공지사항 내용 수정")
    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<NoticeDetailResponse>> updateNotice(
            @PathVariable Long id,
            @RequestPart("request") @Valid UpdateNoticeRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(noticeFacade.updateNotice(id, request, images, principal.getId())));
    }

    @PreAuthorize("hasRole('HQ')")
    @Operation(summary = "공지사항 삭제", description = "특정 공지사항 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(
            @PathVariable Long id
    ) {
        noticeFacade.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
