package com.fundoonotes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundoonotes.dto.request.NoteRequest;
import com.fundoonotes.dto.response.NoteResponse;
import com.fundoonotes.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoteControllerTest {

    private MockMvc mockMvc;
    private NoteService noteService;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        noteService = Mockito.mock(NoteService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new NoteController(noteService)).build();
    }

    @Test
    void createNoteShouldReturnSuccess() throws Exception {
        NoteRequest req = new NoteRequest();
        req.setTitle("My note");
        req.setDescription("desc");
        req.setColor("yellow");

        Mockito.when(noteService.create(Mockito.any(NoteRequest.class)))
                .thenReturn(NoteResponse.builder().id(1L).title("My note").description("desc").isPinned(false).isArchived(false).isTrashed(false).build());

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
