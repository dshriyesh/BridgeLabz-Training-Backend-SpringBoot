package com.fundoonotes.controller;

import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ApiResponse<Object> create(@Valid @RequestBody ReminderRequest request) {
        return ok("Reminder created", reminderService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Object> update(@PathVariable Long id, @Valid @RequestBody ReminderRequest request) {
        return ok("Reminder updated", reminderService.update(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Object> get(@PathVariable Long id) {
        return ok("Reminder fetched", reminderService.get(id));
    }

    @GetMapping
    public ApiResponse<Object> list() {
        return ok("Reminders fetched", reminderService.list());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        reminderService.delete(id);
        return ok("Reminder deleted", null);
    }

    private ApiResponse<Object> ok(String message, Object data) {
        return ApiResponse.builder().success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }
}
