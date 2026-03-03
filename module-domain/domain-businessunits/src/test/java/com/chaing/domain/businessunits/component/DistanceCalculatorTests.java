package com.chaing.domain.businessunits.component;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceCalculatorTests {

    private final DistanceCalculator distanceCalculator = new DistanceCalculator();

    @Test
    @DisplayName("거리 계산 테스트 (10~20km 랜덤)")
    void calculate_RandomDistance() {

        // given
        String address = "서울시 서초구";

        // when
        Double result = distanceCalculator.calculate(address);

        // then
        assertNotNull(result);
        assertTrue(result >= 10.0 && result <= 20.1, "반환된 거리(" + result + ")가 예상 범위(10.0~20.1)를 벗어났습니다.");
        assertEquals(result, Math.round(result * 10.0) / 10.0);
    }
}