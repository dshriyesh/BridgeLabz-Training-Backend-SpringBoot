package com.fundoonotes.dto.response;
import lombok.*; import java.time.LocalDateTime;
import java.util.List;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NoteResponse {
    private Long id;
    private String title;
    private String description;
    private String color;
    private Boolean isPinned;
    private Boolean isArchived;
    private Boolean isTrashed;
    private LocalDateTime reminderTime;
    private List<String> labels;
}
