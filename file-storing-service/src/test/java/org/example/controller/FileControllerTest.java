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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    private final FileDTO testFileDTO = FileDTO.builder()
            .id(1L)
            .name("test.txt")
            .contentType("text/plain")
            .size(11L)
            .hash("testhash")
            .build();

    @Test
    void uploadFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );
        when(fileStorageService.store(any())).thenReturn(testFileDTO);

        mockMvc.perform(multipart("/api/files")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test.txt"))
                .andExpect(jsonPath("$.contentType").value("text/plain"))
                .andExpect(jsonPath("$.size").value(11))
                .andExpect(jsonPath("$.hash").value("testhash"));
    }

    @Test
    void checkFileExistsTrue() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );
        when(fileStorageService.checkFileExists(any())).thenReturn(true);

        mockMvc.perform(multipart("/api/files/check-exists")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void deleteFileSuccess() throws Exception {
        when(fileStorageService.deleteFile(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/files/1"))
                .andExpect(status().isOk());
    }
} 