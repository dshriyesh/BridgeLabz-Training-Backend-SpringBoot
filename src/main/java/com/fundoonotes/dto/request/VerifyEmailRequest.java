package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*;
@Getter @Setter public class VerifyEmailRequest { @NotBlank private String token; }
