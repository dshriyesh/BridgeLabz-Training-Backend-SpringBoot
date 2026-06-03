package com.fundoonotes.controller;

import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping("/users/count")
    public ApiResponse<Object> userCount() {
        return ApiResponse.builder()
                .success(true)
                .message("User count fetched")
                .data(Map.of("count", userRepository.count()))
                .timestamp(LocalDateTime.now())
                .build();
    }
}
