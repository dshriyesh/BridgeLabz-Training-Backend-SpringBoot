package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*;
@Getter @Setter
public class VerifyOtpRequest {
    @NotBlank @Email
    private String email;
    @NotBlank private String otp;
}
