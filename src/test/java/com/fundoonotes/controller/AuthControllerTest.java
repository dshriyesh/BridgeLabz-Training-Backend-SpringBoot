package com.fundoonotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.request.VerifyEmailRequest;
import com.fundoonotes.dto.response.AuthResponse;
import com.fundoonotes.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void loginShouldReturnSuccessResponse() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmailOrUsername("testuser");
        req.setPassword("Password@123");

        Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
                .thenReturn(AuthResponse.builder().accessToken("a").refreshToken("r").tokenType("Bearer").expiresIn(100L).build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("a"));
    }

    @Test
    void verifyEmailGetShouldForwardTokenToService() throws Exception {
        mockMvc.perform(get("/api/v1/auth/verify-email").param("token", "abc-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService, times(1)).verifyEmail(argThat((VerifyEmailRequest r) -> "abc-token".equals(r.getToken())));
    }
}
