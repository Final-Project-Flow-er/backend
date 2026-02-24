package com.chaing.api.controller.notification;

import com.chaing.api.dto.response.NotificationResponse;
import com.chaing.core.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Notification API", description = "알림 API")
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Operation(summary = "알림 목록 조회", description = "로그인한 회원에게 온 알림 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        return ResponseEntity.ok(ApiResponse.success(List.of(NotificationResponse.builder().build())));
    }

    @Operation(summary = "알림 단건 읽음 처리", description = "특정 알림을 읽음 상태로 변경")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "읽지 않은 모든 알림을 한 번에 읽음 처리")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAllNotifications() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "알림 삭제", description = "특정 알림 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
