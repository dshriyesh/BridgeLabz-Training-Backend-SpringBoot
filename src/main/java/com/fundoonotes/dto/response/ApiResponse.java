package com.fundoonotes.dto.response;
import lombok.*; import java.time.LocalDateTime;
@Getter
@Builder
public class ApiResponse<T>{
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
