package com.chaing.external.transport;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/external/transport")
@RequiredArgsConstructor
public class ExternalTransportController {

    private final ExternalTransportService externalTransportService;

    /**
     * 배송 완료 스케줄링 요청 API
     * 프론트엔드에서 출고 승인 후 호출하여 10초 뒤 배송 완료 상태가 되도록 예약함
     */
    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> scheduleDelivery(@RequestBody ScheduleRequest request) {
        if (request.orderCodes() != null && !request.orderCodes().isEmpty()) {
            externalTransportService.scheduleDeliveryCompletion(request.orderCodes());
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    public record ScheduleRequest(List<String> orderCodes) {}
}
