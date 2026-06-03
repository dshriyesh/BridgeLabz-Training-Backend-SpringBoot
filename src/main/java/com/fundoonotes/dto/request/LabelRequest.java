package com.fundoonotes.dto.request;
import jakarta.validation.constraints.NotBlank; import lombok.*;
@Getter @Setter public class LabelRequest { @NotBlank private String name; }
