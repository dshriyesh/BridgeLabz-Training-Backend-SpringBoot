package com.fundoonotes.dto.request;
import jakarta.validation.constraints.NotBlank; import lombok.*;
@Getter @Setter public class RefreshRequest {
    @NotBlank
    private String refreshToken;
}
