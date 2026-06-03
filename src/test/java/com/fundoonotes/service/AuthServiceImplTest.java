package com.fundoonotes.service;

import com.fundoonotes.cache.RedisTokenService;
import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.entity.RefreshToken;
import com.fundoonotes.entity.Role;
import com.fundoonotes.entity.User;
import com.fundoonotes.enums.AccountStatus;
import com.fundoonotes.enums.RoleName;
import com.fundoonotes.exception.UnauthorizedException;
import com.fundoonotes.repository.EmailVerificationTokenRepository;
import com.fundoonotes.repository.RefreshTokenRepository;
import com.fundoonotes.repository.RoleRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.jwt.JwtUtil;
import com.fundoonotes.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class AuthServiceImplTest {

    private AuthServiceImpl authService;
    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private RedisTokenService redisTokenService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtUtil = Mockito.mock(JwtUtil.class);
        redisTokenService = Mockito.mock(RedisTokenService.class);
        EmailVerificationTokenRepository emailVerificationTokenRepository = Mockito.mock(EmailVerificationTokenRepository.class);
        com.fundoonotes.event.EmailEventProducer emailEventProducer = Mockito.mock(com.fundoonotes.event.EmailEventProducer.class);

        authService = new AuthServiceImpl(userRepository, roleRepository, refreshTokenRepository, passwordEncoder, jwtUtil, redisTokenService, emailVerificationTokenRepository, emailEventProducer);
        ReflectionTestUtils.setField(authService, "accessTokenExpiryMs", 604800000L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiryMs", 2592000000L);
    }

    @Test
    void loginShouldThrowWhenEmailNotVerified() {
        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("user");
        request.setPassword("pass");

        User user = User.builder()
                .username("user")
                .email("user@x.com")
                .password("hash")
                .emailVerified(false)
                .accountStatus(AccountStatus.ACTIVE)
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .build();

        Mockito.when(userRepository.findByEmail("user")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("pass", "hash")).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginShouldReturnTokensWhenValid() {
        LoginRequest request = new LoginRequest();
        request.setEmailOrUsername("user");
        request.setPassword("pass");

        User user = User.builder()
                .id(1L)
                .username("user")
                .email("user@x.com")
                .password("hash")
                .emailVerified(true)
                .accountStatus(AccountStatus.ACTIVE)
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .build();

        Mockito.when(userRepository.findByEmail("user")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("pass", "hash")).thenReturn(true);
        Mockito.when(jwtUtil.generateAccessToken("user")).thenReturn("access-token");

        var response = authService.login(request);
        assertEquals("access-token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void logoutShouldBlacklistAccessTokenAndDeleteRefreshToken() {
        User user = User.builder()
                .id(1L)
                .username("user")
                .email("user@x.com")
                .password("hash")
                .emailVerified(true)
                .accountStatus(AccountStatus.ACTIVE)
                .roles(Set.of(Role.builder().name(RoleName.ROLE_USER).build()))
                .build();

        Mockito.when(jwtUtil.extractUsername("access-token")).thenReturn("user");
        Mockito.when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        authService.logout("Bearer access-token");

        verify(redisTokenService).blacklistToken("access-token", 604800000L);
        verify(refreshTokenRepository).deleteByUser(user);
    }
}
