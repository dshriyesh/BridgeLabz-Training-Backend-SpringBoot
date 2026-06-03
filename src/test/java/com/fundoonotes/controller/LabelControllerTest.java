package com.fundoonotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.LabelResponse;
import com.fundoonotes.service.LabelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LabelControllerTest {
    private MockMvc mockMvc;
    private LabelService labelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        labelService = Mockito.mock(LabelService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LabelController(labelService)).build();
    }

    @Test
    void createShouldReturnSuccess() throws Exception {
        LabelRequest req = new LabelRequest();
        req.setName("Work");
        Mockito.when(labelService.create(Mockito.any())).thenReturn(LabelResponse.builder().id(1L).name("Work").build());
        mockMvc.perform(post("/api/v1/labels").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Work"));
    }

    @Test
    void listShouldReturnSuccess() throws Exception {
        Mockito.when(labelService.list()).thenReturn(List.of(LabelResponse.builder().id(1L).name("Work").build()));
        mockMvc.perform(get("/api/v1/labels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
