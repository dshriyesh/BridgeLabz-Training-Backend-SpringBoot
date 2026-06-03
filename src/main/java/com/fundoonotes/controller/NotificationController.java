package com.fundoonotes.controller;

import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Object> list() {
        return ok("Notifications fetched", notificationService.list());
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Object> markRead(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean value) {
        return ok("Notification read status updated", notificationService.markRead(id, value));
    }

    @PutMapping("/read-all")
    public ApiResponse<Object> markAllRead() {
        int updated = notificationService.markAllRead();
        return ok("Notifications marked as read", Map.of("updatedCount", updated));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ok("Notification deleted", null);
    }

    private ApiResponse<Object> ok(String message, Object data) {
        return ApiResponse.builder().success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }
}
