package org.example.controller;

import org.example.dto.FileDTO;
import org.example.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    void uploadFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "test content".getBytes()
        );

        FileDTO responseDTO = FileDTO.builder()
            .id(1L)
            .name("test.txt")
            .contentType(MediaType.TEXT_PLAIN_VALUE)
            .size(12L)
            .hash("testhash")
            .build();

        when(fileStorageService.uploadFile(any())).thenReturn(responseDTO);

        mockMvc.perform(multipart("/api/files")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("test.txt"))
            .andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
            .andExpect(jsonPath("$.size").value(12))
            .andExpect(jsonPath("$.hash").value("testhash"));
    }
} 