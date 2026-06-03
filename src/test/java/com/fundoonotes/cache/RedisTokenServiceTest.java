package com.fundoonotes.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RedisTokenServiceTest {
    private RedisTokenService redisTokenService;
    private StringRedisTemplate template;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        template = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        Mockito.when(template.opsForValue()).thenReturn(valueOperations);
        redisTokenService = new RedisTokenService(template);
    }

    @Test
    void otpShouldMatchStoredValue() {
        Mockito.when(valueOperations.get("auth:otp:test@mail.com")).thenReturn("123456");
        assertTrue(redisTokenService.otpMatches("test@mail.com", "123456"));
        assertFalse(redisTokenService.otpMatches("test@mail.com", "654321"));
    }

    @Test
    void activeTokenKeyCheckShouldReturnTrue() {
        Mockito.when(template.hasKey("auth:active:t")).thenReturn(true);
        assertTrue(redisTokenService.isTokenActive("t"));
    }

    @Test
    void storeActiveTokenShouldWriteWithExpiry() {
        redisTokenService.storeActiveToken("t", 1000L);
        Mockito.verify(valueOperations).set("auth:active:t", "1", Duration.ofMillis(1000L));
    }
}
