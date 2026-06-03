package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*;

@Getter @Setter
public class ResetPasswordRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String otp;

    @NotBlank
    @Size(min=8)
    private String newPassword;
}
