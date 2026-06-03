package com.fundoonotes.security;

import com.fundoonotes.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void shouldGenerateAndValidateToken() {
        String secret = "1234567890123456789012345678901234567890123456789012345678901234";
        JwtUtil jwtUtil = new JwtUtil(secret, 60000);
        String token = jwtUtil.generateAccessToken("alice");

        assertNotNull(token);
        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("alice", jwtUtil.extractUsername(token));
    }
}
