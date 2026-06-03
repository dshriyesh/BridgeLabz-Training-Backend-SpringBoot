package com.fundoonotes.controller;

import com.fundoonotes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest {
    private MockMvc mockMvc;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminController(userRepository)).build();
    }

    @Test
    void userCountShouldReturnCount() throws Exception {
        Mockito.when(userRepository.count()).thenReturn(5L);
        mockMvc.perform(get("/api/v1/admin/users/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(5));
    }
}
