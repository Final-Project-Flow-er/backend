package com.chaing.core.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class RedisSequenceGenerator {

    private final StringRedisTemplate redisTemplate;

    private static final long MAX_SEQUENCE = 99999L;

    public String nextSequence(String prefix) {
        String today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String key = "seq:" + prefix + ":" + today;

        Long seq = redisTemplate.opsForValue().increment(key);

        if (seq != null && seq == 1L) {
            redisTemplate.expire(key, Duration.ofHours(48));
        }

        if (seq == null || seq > MAX_SEQUENCE) {
            throw new IllegalStateException("일련번호 초과: " + key);
        }

        return String.format("%05d", seq);
    }
}