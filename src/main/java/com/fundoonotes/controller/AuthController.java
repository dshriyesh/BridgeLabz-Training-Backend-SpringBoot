package com.fundoonotes.controller;

import com.fundoonotes.dto.request.*;
import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Object> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ok("Registration initiated");
    }

    @PostMapping("/login")
    public ApiResponse<Object> login(@Valid @RequestBody LoginRequest request) {
        return okWithData("Login success", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<Object> refresh(@Valid @RequestBody RefreshRequest request) { return okWithData("Token refreshed", authService.refresh(request)); }
    @PostMapping("/logout")
    public ApiResponse<Object> logout(@RequestHeader("Authorization") String authHeader) { authService.logout(authHeader); return ok("Logout success"); }
    @PostMapping("/forgot-password")
    public ApiResponse<Object> forgot(@Valid @RequestBody ForgotPasswordRequest request) { authService.forgotPassword(request); return ok("OTP sent"); }
    @PostMapping("/verify-otp")
    public ApiResponse<Object> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) { authService.verifyOtp(request); return ok("OTP verified"); }
    @PostMapping("/reset-password")
    public ApiResponse<Object> resetPassword(@Valid @RequestBody ResetPasswordRequest request) { authService.resetPassword(request); return ok("Password reset"); }
    @PostMapping("/verify-email")
    public ApiResponse<Object> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) { authService.verifyEmail(request); return ok("Email verified"); }
    @GetMapping("/verify-email")
    public ApiResponse<Object> verifyEmailByQuery(@RequestParam("token") String token) {
        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken(token);
        authService.verifyEmail(request);
        return ok("Email verified");
    }

    private ApiResponse<Object> ok(String message)
    {
        return ApiResponse.builder().success(true).message(message).timestamp(LocalDateTime.now()).data(null).build();
    }
    private ApiResponse<Object> okWithData(String message, Object data){
        return ApiResponse.builder().success(true).message(message).timestamp(LocalDateTime.now()).data(data).build();
    }
}
