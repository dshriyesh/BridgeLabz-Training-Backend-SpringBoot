package com.fundoonotes.controller;

import com.fundoonotes.dto.response.FileResponse;
import com.fundoonotes.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FileControllerTest {
    private MockMvc mockMvc;
    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = Mockito.mock(FileService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new FileController(fileService)).build();
    }

    @Test
    void uploadShouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", "abc".getBytes());
        Mockito.when(fileService.upload(Mockito.any(), Mockito.anyLong())).thenReturn(FileResponse.builder().id(1L).fileName("a.jpg").fileType("jpg").noteId(1L).build());
        mockMvc.perform(multipart("/api/v1/files/upload").file(file).param("noteId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/files/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void listByNoteShouldReturnSuccess() throws Exception {
        Mockito.when(fileService.listByNote(1L)).thenReturn(List.of(FileResponse.builder().id(1L).fileName("a.jpg").fileType("jpg").noteId(1L).build()));
        mockMvc.perform(get("/api/v1/files/note/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
