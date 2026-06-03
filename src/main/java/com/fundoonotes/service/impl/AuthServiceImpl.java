package com.fundoonotes.service.impl;

import com.fundoonotes.cache.RedisTokenService;
import com.fundoonotes.dto.request.*;
import com.fundoonotes.dto.response.AuthResponse;
import com.fundoonotes.entity.EmailVerificationToken;
import com.fundoonotes.entity.RefreshToken;
import com.fundoonotes.entity.Role;
import com.fundoonotes.entity.User;
import com.fundoonotes.enums.AccountStatus;
import com.fundoonotes.enums.RoleName;
import com.fundoonotes.event.EmailEventProducer;
import com.fundoonotes.exception.EmailAlreadyExistsException;
import com.fundoonotes.exception.InvalidTokenException;
import com.fundoonotes.exception.OTPExpiredException;
import com.fundoonotes.exception.UnauthorizedException;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.RefreshTokenRepository;
import com.fundoonotes.repository.RoleRepository;
import com.fundoonotes.repository.EmailVerificationTokenRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.jwt.JwtUtil;
import com.fundoonotes.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailEventProducer emailEventProducer;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Value("${app.otp.expiry-seconds:300}")
    private long otpExpirySeconds;

    @Value("${app.email-verification.expiry-hours:24}")
    private long verificationExpiryHours;

    @Value("${app.email-verification.base-url:http://localhost:8080/api/v1/auth/verify-email?token=}")
    private String emailVerificationBaseUrl;

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new EmailAlreadyExistsException("Username already exists");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(false)
                .roles(Set.of(userRole))
                .build();
        User savedUser = userRepository.save(user);

        emailVerificationTokenRepository.deleteByUser(savedUser);
        String verificationToken = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .token(verificationToken)
                .expiryDate(LocalDateTime.now().plusHours(verificationExpiryHours))
                .user(savedUser)
                .build());

        String verificationUrl = emailVerificationBaseUrl + verificationToken;
        emailEventProducer.publish(EmailNotificationEvent.builder()
                .to(savedUser.getEmail())
                .subject("Verify your Fundoo Notes account")
                .body("Welcome to Fundoo Notes.\n\nPlease verify your email using this link:\n" + verificationUrl + "\n\nThis link expires in " + verificationExpiryHours + " hours.")
                .build());
        log.info("User registered and verification event queued: {}", savedUser.getEmail());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmailOrUsername())
                .or(() -> userRepository.findByUsername(request.getEmailOrUsername()))
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new UnauthorizedException("Please verify your email before login");
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        redisTokenService.storeActiveToken(accessToken, accessTokenExpiryMs);

        refreshTokenRepository.deleteByUser(user);
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .user(user)
                .build());

        log.info("User logged in: {}", user.getUsername());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMs / 1000)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (stored.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw new InvalidTokenException("Refresh token expired");
        }

        String accessToken = jwtUtil.generateAccessToken(stored.getUser().getUsername());
        redisTokenService.storeActiveToken(accessToken, accessTokenExpiryMs);

        String rotatedRefreshToken = UUID.randomUUID().toString();
        stored.setToken(rotatedRefreshToken);
        stored.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000));
        refreshTokenRepository.save(stored);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rotatedRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiryMs / 1000)
                .build();
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        String principal;
        try {
            principal = jwtUtil.extractUsername(token);
        } catch (Exception ex) {
            throw new UnauthorizedException("Invalid access token");
        }
        User user = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
        redisTokenService.blacklistToken(token, accessTokenExpiryMs);
        refreshTokenRepository.deleteByUser(user);
        log.info("Token blacklisted on logout");
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        redisTokenService.storeOtp(user.getEmail(), otp, otpExpirySeconds);
        emailEventProducer.publish(EmailNotificationEvent.builder()
                .to(user.getEmail())
                .subject("Fundoo Notes password reset OTP")
                .body("Your OTP for password reset is: " + otp + "\n\nThis OTP expires in " + (otpExpirySeconds / 60) + " minutes.")
                .build());
        log.info("Generated OTP and queued email for {}", user.getEmail());
    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));
        String storedOtp = redisTokenService.getOtp(request.getEmail());
        if (storedOtp == null) {
            throw new OTPExpiredException("OTP expired. Please request a new OTP.");
        }
        if (!storedOtp.equals(request.getOtp())) {
            throw new ValidationException("Invalid OTP");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));
        String storedOtp = redisTokenService.getOtp(request.getEmail());
        if (storedOtp == null) {
            throw new OTPExpiredException("OTP expired. Please request a new OTP.");
        }
        if (!storedOtp.equals(request.getOtp())) {
            throw new ValidationException("Invalid OTP");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
        redisTokenService.deleteOtp(request.getEmail());
        log.info("Password reset successful for {}", request.getEmail());
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            emailVerificationTokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Verification token expired");
        }
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);
        log.info("Email verified for {}", user.getEmail());
    }
}
