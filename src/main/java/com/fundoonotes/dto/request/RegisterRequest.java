package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*;
@Getter @Setter
public class RegisterRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @NotBlank private String username;
    @NotBlank @Size(min=8) private String password;
}
