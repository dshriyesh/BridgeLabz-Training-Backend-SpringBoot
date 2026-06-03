package com.fundoonotes.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {
    private Long id;
    private Long noteId;
    private LocalDateTime reminderTime;
    private Boolean notified;
}
