package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*;

@Getter @Setter
public class ForgotPasswordRequest {
    @NotBlank
    @Email
    private String email;
}
