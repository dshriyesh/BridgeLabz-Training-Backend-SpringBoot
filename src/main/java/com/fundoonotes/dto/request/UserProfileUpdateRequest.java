package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*;
@Getter @Setter
public class UserProfileUpdateRequest
{
    @NotBlank private String firstName;
    @NotBlank private String lastName;
}
