package com.fundoonotes.service.impl;

import com.fundoonotes.dto.request.ChangePasswordRequest;
import com.fundoonotes.dto.request.UserProfileUpdateRequest;
import com.fundoonotes.dto.response.UserProfileResponse;
import com.fundoonotes.entity.User;
import com.fundoonotes.exception.UserNotFoundException;
import com.fundoonotes.exception.ValidationException;
import com.fundoonotes.repository.RefreshTokenRepository;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "png");

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.storage.path:uploads}")
    private String storagePath;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Override
    public UserProfileResponse getProfile() {

        return toResponse(getCurrentUser());
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserProfileResponse uploadProfileImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Profile image file is required");
        }
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("Only jpg and png profile images are allowed");
        }

        User user = getCurrentUser();
        Path uploadDir = Paths.get(storagePath, "profile-images").toAbsolutePath().normalize();
        String generated = "profile_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + "_" + UUID.randomUUID() + "." + extension;
        Path target = uploadDir.resolve(generated).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new ValidationException("Invalid profile image path");
        }
        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ValidationException("Failed to save profile image: " + ex.getMessage());
        }
        user.setProfileImage(target.toString());
        log.info("Updated profile image for user {}", user.getUsername());
        return toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidationException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.deleteByUser(user);
        log.info("Password changed for user {}", user.getUsername());
    }

    @Override
    public Resource getProfileImage() {
        User user = getCurrentUser();
        if (user.getProfileImage() == null || user.getProfileImage().isBlank()) {
            throw new ValidationException("Profile image not found");
        }
        Path path = Paths.get(user.getProfileImage()).toAbsolutePath().normalize();
        Path uploadDir = Paths.get(storagePath).toAbsolutePath().normalize();
        if (!path.startsWith(uploadDir)) {
            throw new ValidationException("Invalid profile image path");
        }
        Resource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ValidationException("Profile image does not exist on server");
        }
        return resource;
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new ValidationException("Invalid file name");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private User getCurrentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .profileImage(user.getProfileImage())
                .profileImageUrl(user.getProfileImage() == null || user.getProfileImage().isBlank() ? null : publicBaseUrl + "/api/v1/users/profile-image")
                .accountStatus(user.getAccountStatus().name())
                .roles(user.getRoles().stream().map(r -> r.getName().name()).collect(Collectors.toSet()))
                .build();
    }
}
