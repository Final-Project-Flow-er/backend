package com.chaing.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = "com.chaing") // 패키지 전체 스캔
@EntityScan(basePackages = "com.chaing")                // 타 모듈 @Entity 전부 탐색
@EnableJpaRepositories(basePackages = "com.chaing")     // 다른 모듈에 있는 Repository 전부 탐색
@EnableRedisRepositories(basePackages = "com.chaing")   // Redis용 레포지토리 탐색 활성화
public class ChaingApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChaingApiApplication.class, args);
    }
}