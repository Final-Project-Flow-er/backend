package com.chaing.api.controller.notification;

import com.chaing.api.dto.notification.response.NotificationListResponse;
import com.chaing.api.facade.notification.NotificationFacade;
import com.chaing.api.security.principal.UserPrincipal;
import com.chaing.core.dto.ApiResponse;
import com.chaing.domain.notifications.enums.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 API")
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationFacade notificationFacade;

    @Operation(summary = "실시간 알림 스트림", description = "SSE 연결을 통한 실시간 알림 수신")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(notificationFacade.stream(principal.getId()));
    }

    @Operation(summary = "알림 목록 조회", description = "로그인한 회원에게 온 알림 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationListResponse>>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) NotificationType type,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationFacade.getNotificationList(principal.getId(), type, pageable)));
    }

    @Operation(summary = "미읽음 알림 수 조회", description = "읽지 않은 알림의 개수를 반환")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(notificationFacade.getUnreadCount(principal.getId())));
    }

    @Operation(summary = "알림 단건 읽음 처리", description = "특정 알림을 읽음 상태로 변경")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationListResponse>> readNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ApiResponse.success(notificationFacade.readNotification(id, principal.getId())));
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "읽지 않은 모든 알림을 한 번에 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAllNotifications(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        notificationFacade.markAllAsRead(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "알림 삭제", description = "특정 알림 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        notificationFacade.deleteNotification(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
