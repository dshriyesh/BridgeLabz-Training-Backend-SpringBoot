package com.fundoonotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoonotes.dto.request.ChangePasswordRequest;
import com.fundoonotes.dto.request.UserProfileUpdateRequest;
import com.fundoonotes.dto.response.UserProfileResponse;
import com.fundoonotes.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {
    private MockMvc mockMvc;
    private UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService)).build();
    }

    @Test
    void getProfileShouldReturnSuccess() throws Exception {
        Mockito.when(userService.getProfile()).thenReturn(UserProfileResponse.builder().id(1L).username("u").roles(Set.of("ROLE_USER")).build());
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateProfileShouldReturnSuccess() throws Exception {
        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        Mockito.when(userService.updateProfile(Mockito.any())).thenReturn(UserProfileResponse.builder().id(1L).firstName("John").lastName("Doe").build());
        mockMvc.perform(put("/api/v1/users/profile").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("John"));
    }

    @Test
    void uploadProfileImageShouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "p.jpg", "image/jpeg", "abc".getBytes());
        Mockito.when(userService.uploadProfileImage(Mockito.any())).thenReturn(UserProfileResponse.builder().id(1L).profileImage("x").build());
        mockMvc.perform(multipart("/api/v1/users/upload-profile-image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changePasswordShouldReturnSuccess() throws Exception {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("OldPassword@123");
        req.setNewPassword("NewPassword@123");

        mockMvc.perform(put("/api/v1/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getProfileImageShouldReturnResource() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("abc".getBytes()) {
            @Override
            public String getFilename() {
                return "profile.jpg";
            }
        };
        Mockito.when(userService.getProfileImage()).thenReturn(resource);

        mockMvc.perform(get("/api/v1/users/profile-image"))
                .andExpect(status().isOk());
    }
}
