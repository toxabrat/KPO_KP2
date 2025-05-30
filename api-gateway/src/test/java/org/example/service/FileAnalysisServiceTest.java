package org.example.service;

import org.example.dto.FileAnalysisDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileAnalysisServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FileAnalysisService fileAnalysisService;

    private static final String FILE_ANALYSIS_URL = "http://file-analysis-service:8082";
    private static final String FILE_ID = "test-file-1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileAnalysisService, "fileAnalysisUrl", FILE_ANALYSIS_URL);
    }

    @Test
    void analyzeFileSuccess() {
        FileAnalysisDTO expectedResult = FileAnalysisDTO.builder()
            .fileId(FILE_ID)
            .paragraphCount(2)
            .wordCount(10)
            .characterCount(50)
            .build();

        when(restTemplate.postForEntity(
            eq(FILE_ANALYSIS_URL + "/api/analysis/files/" + FILE_ID + "/analyze"),
            eq(null),
            eq(FileAnalysisDTO.class)
        )).thenReturn(new ResponseEntity<>(expectedResult, HttpStatus.OK));

        FileAnalysisDTO result = fileAnalysisService.analyzeFile(FILE_ID);
        assertNotNull(result);
        assertEquals(FILE_ID, result.getFileId());
        assertEquals(2, result.getParagraphCount());
        assertEquals(10, result.getWordCount());
        assertEquals(50, result.getCharacterCount());
    }
} 