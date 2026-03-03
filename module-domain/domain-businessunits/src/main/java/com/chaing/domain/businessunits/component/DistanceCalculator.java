package com.chaing.domain.businessunits.component;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DistanceCalculator {

    public Double calculate(String address) {
        try {
            return fetchActualDistance(address);
        } catch (Exception e) {
            double randomDistance = ThreadLocalRandom.current().nextDouble(10.0, 20.1);
            return Math.round(randomDistance * 10.0) / 10.0;
        }
    }

    private Double fetchActualDistance(String address) {
        // TODO: 실제 거리 계산 로직 구현 예정
        throw new UnsupportedOperationException("실제 거리 계산 API가 아직 구현되지 않았습니다.");
    }
}
