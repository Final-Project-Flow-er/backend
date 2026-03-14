package com.chaing.external.transport;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 외부 운송 모듈
 * 송장 번호 생성 및 관리를 담당하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class ExternalTransportService {

    private final TaskScheduler taskScheduler;

    // orderCode와 trackingNumber를 1:1로 매핑하여 저장하는 저장소
    private final Map<String, String> trackingStorage = new HashMap<>();

    // 이미 할당된 전체 송장 번호를 관리하는 Set (중복 방지용)
    private final Set<String> assignedTrackingNumbers = new HashSet<>();

    /**
     * 단일 발주 코드(orderCode)에 대한 송장 번호를 생성하고 저장한 뒤 반환
     * 
     * @param orderCode 발주 코드
     * @return 생성된 송장 번호 (trackingNumber)
     */
    public String createTrackingNumber(String orderCode) {
        // 이미 생성된 송장 번호가 있는 경우 기존 번호 반환
        if (trackingStorage.containsKey(orderCode)) {
            return trackingStorage.get(orderCode);
        }

        // 새로운 송장 번호 생성 및 중복 검증
        String trackingNumber;
        do {
            // 'T' + 숫자 12자리 (총 13자리) 생성
            StringBuilder sb = new StringBuilder("T");
            for (int i = 0; i < 12; i++) {
                sb.append(ThreadLocalRandom.current().nextInt(10));
            }
            trackingNumber = sb.toString();
            
            // 이미 할당된 번호인지 확인하여 중복되면 재생성
        } while (assignedTrackingNumbers.contains(trackingNumber));

        // 중복되지 않은 번호인 경우 관리 목록에 추가 및 저장
        assignedTrackingNumbers.add(trackingNumber);
        trackingStorage.put(orderCode, trackingNumber);
        
        return trackingNumber;
    }

    /**
     * 여러 발주 코드 리스트를 받아 각 발주별 송장 번호가 포함된 Map을 반환
     * InternalTransportFacade 등에서 외부 운송 모듈 연동 시 사용
     * 
     * @param orderCodes 발주 코드 리스트
     * @return orderCode를 키로 하고 trackingNumber를 값으로 하는 Map
     */
    public Map<String, String> getTrackingNumbers(List<String> orderCodes) {
        return orderCodes.stream()
                .collect(Collectors.toMap(
                        orderCode -> orderCode,
                        this::createTrackingNumber,
                        (existing, replacement) -> existing // 중복 코드가 있을 경우 기존 값 유지
                ));
    }

    /**
     * 배송 시작 시 호출하며, 10초 뒤에 배송 완료 상태 변경을 요청하도록 스케줄링
     * 
     * @param trackingNumber 송장 번호
     */
    public void scheduleDeliveryCompletion(String trackingNumber) {
        taskScheduler.schedule(() -> {
            requestDeliveryCompletion(trackingNumber);
        }, Instant.now().plusSeconds(10));
    }

    /**
     * 실제 배송 완료 상태 변경을 요청하는 로직
     * 현재는 시뮬레이션을 위해 로그를 출력하며, 추후 메인 시스템의 API 호출 로직이 들어갈 자리
     * 
     * @param trackingNumber 송장 번호
     */
    private void requestDeliveryCompletion(String trackingNumber) {
        // 실제 운영 환경에서는 여기서 메인 서버의 API를 호출하거나 이벤트를 발행
        System.out.println("[외부 운송 시스템] 송장 번호: " + trackingNumber + " - 배송 완료 상태 변경 요청 전송 (10초 경과)");
    }
}
