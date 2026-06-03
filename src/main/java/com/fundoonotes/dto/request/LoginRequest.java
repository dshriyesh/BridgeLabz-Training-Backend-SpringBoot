package com.fundoonotes.dto.request;
import jakarta.validation.constraints.NotBlank; import lombok.*;
@Getter @Setter
public class LoginRequest {
    @NotBlank private String emailOrUsername;
    @NotBlank private String password;
}
