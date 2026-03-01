package com.chaing.domain.users.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "refreshToken")
public class RefreshToken {

    @Id
    private Long userId;

    @Indexed
    private String token;

    @TimeToLive
    private Long expiration;

    public void updateToken(String token, Long expiration) {
        this.token = token;
        this.expiration = expiration;
    }

    public static RefreshToken create(Long userId, String token, Long expiration) {
        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiration(expiration)
                .build();
    }
}
