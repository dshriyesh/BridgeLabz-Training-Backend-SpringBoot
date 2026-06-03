package com.fundoonotes.dto.request;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailNotificationEvent implements Serializable {
    private String to;
    private String subject;
    private String body;
}
