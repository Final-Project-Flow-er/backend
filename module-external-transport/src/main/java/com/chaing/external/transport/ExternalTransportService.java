package com.chaing.external.transport;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

/**
 * 외부 운송 모듈
 * 송장 번호 생성 및 관리를 담당하는 서비스 클래스
 */
@Service
public class ExternalTransportService {

    private final TaskScheduler taskScheduler;
    private final RestTemplate restTemplate;
    private final String mainServerUrl;

    // orderCode와 trackingNumber를 1:1로 매핑하여 저장하는 저장소
    private final Map<String, String> trackingStorage = new ConcurrentHashMap<>();

    // trackingNumber를 통해 orderCode를 찾기 위한 역방향 저장소
    private final Map<String, String> reverseTrackingStorage = new ConcurrentHashMap<>();

    // 이미 할당된 전체 송장 번호를 관리하는 Set (중복 방지용)
    private final Set<String> assignedTrackingNumbers = ConcurrentHashMap.newKeySet();

    public ExternalTransportService(TaskScheduler taskScheduler,
            @Value("${external.transport.main-server-url:http://localhost:8080}") String mainServerUrl) {
        this.taskScheduler = taskScheduler;
        this.mainServerUrl = mainServerUrl;
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(3000))
                        .setResponseTimeout(Timeout.ofMilliseconds(5000))
                        .build())
                .build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    /**
     * 배송 상태 변경 요청을 위한 내부 DTO
     */
    private record UpdateDeliverStatusRequest(List<String> orderCodes) {}

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

        // (송장 번호 생성 loop 생략)

        // 고유 번호 할당 및 저장
        assignedTrackingNumbers.add(trackingNumber);
        trackingStorage.put(orderCode, trackingNumber);
        reverseTrackingStorage.put(trackingNumber, orderCode);
        
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
     * @param orderCodes 발주 코드 리스트
     */
    public void scheduleDeliveryCompletion(List<String> orderCodes) {
        orderCodes.forEach(orderCode -> {
            String trackingNumber = trackingStorage.get(orderCode);
            if (trackingNumber != null) {
                taskScheduler.schedule(() -> {
                    requestDeliveryCompletion(trackingNumber);
                }, Instant.now().plusSeconds(10));
            } else {
                System.err.println("[외부 운송 시스템] 오류: 발주 코드에 해당하는 송장 번호를 찾을 수 없음 - " + orderCode);
            }
        });
    }

    /**
     * 실제 배송 완료 상태 변경을 요청하는 로직
     * 메인 서버의 API를 호출하여 상태를 변경합니다.
     * 
     * @param trackingNumber 송장 번호
     */
    private void requestDeliveryCompletion(String trackingNumber) {
        String orderCode = reverseTrackingStorage.get(trackingNumber);
        if (orderCode == null) {
            System.err.println("[외부 운송 시스템] 오류: 송장 번호에 해당하는 발주 코드를 찾을 수 없음 - " + trackingNumber);
            return;
        }

        // 메인 서버 API 엔드포인트 설정 (자기 자신 호출)
        String url = mainServerUrl + "/api/v1/transport/internal/updated-deliver-status";

        // 요청 바디 생성 (List<String> orderCodes)
        UpdateDeliverStatusRequest requestBody = new UpdateDeliverStatusRequest(Collections.singletonList(orderCode));
        
        try {
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpdateDeliverStatusRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    requestEntity,
                    Void.class
            );

            System.out.println("[외부 운송 시스템] 송장 번호: " + trackingNumber
                    + " (발주 번호: " + orderCode + ") - 메인 서버 API 호출 완료, status=" + response.getStatusCode());        } catch (Exception e) {
            System.err.println("[외부 운송 시스템] 메인 서버 API 호출 실패: " + e.getMessage());
        }
    }
}
