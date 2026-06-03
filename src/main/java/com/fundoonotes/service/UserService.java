package com.fundoonotes.service;

import com.fundoonotes.dto.request.ChangePasswordRequest;
import com.fundoonotes.dto.request.UserProfileUpdateRequest;
import com.fundoonotes.dto.response.UserProfileResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getProfile();
    UserProfileResponse updateProfile(UserProfileUpdateRequest request);
    UserProfileResponse uploadProfileImage(MultipartFile file);
    void changePassword(ChangePasswordRequest request);
    Resource getProfileImage();
}
