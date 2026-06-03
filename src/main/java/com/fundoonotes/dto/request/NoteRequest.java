package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*;
import java.util.Set;
@Getter @Setter

public class NoteRequest {
    @NotBlank private String title;
    private String description;
    private String color;
    private Long labelId;
    private Set<Long> labelIds;
}
