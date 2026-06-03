package com.fundoonotes.security;

import com.fundoonotes.cache.RedisTokenService;
import com.fundoonotes.security.filter.JwtAuthenticationFilter;
import com.fundoonotes.security.jwt.JwtUtil;
import com.fundoonotes.security.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private JwtUtil jwtUtil;
    private RedisTokenService redisTokenService;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        redisTokenService = mock(RedisTokenService.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtUtil, redisTokenService, userDetailsService);
        chain = mock(FilterChain.class);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipWhenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        HttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAuthenticateWhenTokenValidAndActive() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer abc");
        HttpServletResponse res = new MockHttpServletResponse();

        when(jwtUtil.isTokenValid("abc")).thenReturn(true);
        when(redisTokenService.isBlacklisted("abc")).thenReturn(false);
        when(redisTokenService.isTokenActive("abc")).thenReturn(true);
        when(jwtUtil.extractUsername("abc")).thenReturn("alice");
        when(userDetailsService.loadUserByUsername("alice")).thenReturn(
                org.springframework.security.core.userdetails.User.withUsername("alice").password("").authorities("ROLE_USER").build()
        );

        filter.doFilter(req, res, chain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
