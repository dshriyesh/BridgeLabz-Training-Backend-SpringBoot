package com.fundoonotes.dto.request;
import jakarta.validation.constraints.*; import lombok.*; import java.time.LocalDateTime;
@Getter @Setter
public class ReminderRequest {
    @NotNull private Long noteId;
    @NotNull private LocalDateTime reminderTime;
}
