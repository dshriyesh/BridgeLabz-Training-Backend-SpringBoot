package com.fundoonotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoonotes.dto.request.ReminderRequest;
import com.fundoonotes.dto.response.ReminderResponse;
import com.fundoonotes.service.ReminderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReminderControllerTest {
    private MockMvc mockMvc;
    private ReminderService reminderService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        reminderService = Mockito.mock(ReminderService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReminderController(reminderService)).build();
    }

    @Test
    void createShouldReturnSuccess() throws Exception {
        ReminderRequest request = new ReminderRequest();
        request.setNoteId(1L);
        request.setReminderTime(LocalDateTime.now().plusHours(1));
        Mockito.when(reminderService.create(Mockito.any())).thenReturn(ReminderResponse.builder().id(1L).noteId(1L).notified(false).build());
        mockMvc.perform(post("/api/v1/reminders").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/reminders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateGetListShouldReturnSuccess() throws Exception {
        ReminderRequest request = new ReminderRequest();
        request.setNoteId(1L);
        request.setReminderTime(LocalDateTime.now().plusHours(2));

        Mockito.when(reminderService.update(Mockito.eq(1L), Mockito.any()))
                .thenReturn(ReminderResponse.builder().id(1L).noteId(1L).notified(false).build());
        Mockito.when(reminderService.get(1L))
                .thenReturn(ReminderResponse.builder().id(1L).noteId(1L).notified(false).build());
        Mockito.when(reminderService.list())
                .thenReturn(List.of(ReminderResponse.builder().id(1L).noteId(1L).notified(false).build()));

        mockMvc.perform(put("/api/v1/reminders/1").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/reminders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/reminders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
