package com.fundoonotes.controller;

import com.fundoonotes.dto.request.ChangePasswordRequest;
import com.fundoonotes.dto.request.UserProfileUpdateRequest;
import com.fundoonotes.dto.response.ApiResponse;
import com.fundoonotes.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<Object> getProfile() {
        return ok("Profile fetched", userService.getProfile());
    }

    @PutMapping("/profile")
    public ApiResponse<Object> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        return ok("Profile updated", userService.updateProfile(request));
    }

    @PutMapping("/change-password")
    public ApiResponse<Object> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ok("Password changed", null);
    }

    @PostMapping(value = "/upload-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Object> uploadProfileImage(@RequestPart("file") MultipartFile file) {
        return ok("Profile image uploaded", userService.uploadProfileImage(file));
    }

    @GetMapping("/profile-image")
    public ResponseEntity<Resource> getProfileImage() {
        Resource resource = userService.getProfileImage();
        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(Path.of(resource.getFile().getAbsolutePath()));
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
                .body(resource);
    }

    private ApiResponse<Object> ok(String message, Object data) {
        return ApiResponse.builder().success(true).message(message).data(data).timestamp(LocalDateTime.now()).build();
    }
}
