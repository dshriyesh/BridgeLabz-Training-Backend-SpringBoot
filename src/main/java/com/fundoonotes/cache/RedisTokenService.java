package com.fundoonotes.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    public void storeActiveToken(String token, long expiryMs) {
        redisTemplate.opsForValue().set("auth:active:" + token, "1", Duration.ofMillis(expiryMs));
    }

    public boolean isTokenActive(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("auth:active:" + token));
    }

    public void blacklistToken(String token, long expiryMs) {
        redisTemplate.opsForValue().set("auth:blacklist:" + token, "1", Duration.ofMillis(expiryMs));
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("auth:blacklist:" + token));
    }

    public void storeOtp(String email, String otp, long expirySeconds) {
        redisTemplate.opsForValue().set("auth:otp:" + email, otp, Duration.ofSeconds(expirySeconds));
    }

    public String getOtp(String email) {
        return redisTemplate.opsForValue().get("auth:otp:" + email);
    }

    public boolean otpMatches(String email, String otp) {
        return Objects.equals(getOtp(email), otp);
    }

    public void deleteOtp(String email) {
        redisTemplate.delete("auth:otp:" + email);
    }
}
