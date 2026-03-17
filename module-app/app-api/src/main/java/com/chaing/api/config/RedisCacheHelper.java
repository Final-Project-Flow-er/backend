package com.chaing.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisCacheHelper {

    private final StringRedisTemplate redisTemplate;

    public void evictByPattern(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            List<String> batch = new ArrayList<>();
            while (cursor.hasNext()) {
                batch.add(cursor.next());
                if (batch.size() >= 100) {
                    redisTemplate.unlink(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                redisTemplate.unlink(batch);
            }
        } catch (Exception ignored) {
        }
    }
}
