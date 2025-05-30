package org.example.controller;

import org.example.dto.FileAnalysisDTO;
import org.example.service.FileAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileAnalysisController.class)
class FileAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileAnalysisService fileAnalysisService;

    @Test
    void analyzeFileSuccess() throws Exception {
        String fileId = "test-file-1";
        FileAnalysisDTO analysisDTO = FileAnalysisDTO.builder()
            .fileId(fileId)
            .paragraphCount(2)
            .wordCount(10)
            .characterCount(50)
            .build();

        when(fileAnalysisService.analyzeFile(fileId)).thenReturn(analysisDTO);

        mockMvc.perform(post("/api/analysis/files/{fileId}/analyze", fileId))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.fileId").value(fileId))
            .andExpect(jsonPath("$.paragraphCount").value(2))
            .andExpect(jsonPath("$.wordCount").value(10))
            .andExpect(jsonPath("$.characterCount").value(50));
    }
} 