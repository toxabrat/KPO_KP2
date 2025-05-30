package org.example.service;

import org.example.dto.FileAnalysisDTO;
import org.example.repository.AnalysisResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileAnalysisServiceTest {

    @Mock
    private AnalysisResultRepository analysisResultRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FileAnalysisService fileAnalysisService;

    private static final String FILE_ID = "test-file-1";
    private static final String FILE_STORAGE_URL = "http://file-storing-service:8080";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileAnalysisService, "fileStorageUrl", FILE_STORAGE_URL);
        ReflectionTestUtils.setField(fileAnalysisService, "restTemplate", restTemplate);
    }

    @Test
    void analyzeFileEmptyContentThrowsException() {
        String fileUrl = FILE_STORAGE_URL + "/api/files/" + FILE_ID + "/content";
        
        when(analysisResultRepository.findById(FILE_ID)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(fileUrl, String.class)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> fileAnalysisService.analyzeFile(FILE_ID));
        
        verify(analysisResultRepository).findById(FILE_ID);
        verify(restTemplate).getForObject(fileUrl, String.class);
        verify(analysisResultRepository, never()).save(any());
    }

    @Test
    void analyzeFileComplexTextCorrectCounts() {
        String fileContent = "first\n\n" +
                           "second\n\n" +
                           "three";
        String fileUrl = FILE_STORAGE_URL + "/api/files/" + FILE_ID + "/content";
        
        when(analysisResultRepository.findById(FILE_ID)).thenReturn(Optional.empty());
        when(restTemplate.getForObject(fileUrl, String.class)).thenReturn(fileContent);
        when(analysisResultRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        FileAnalysisDTO result = fileAnalysisService.analyzeFile(FILE_ID);

        assertNotNull(result);
        assertEquals(3, result.getParagraphCount());
        assertEquals(3, result.getWordCount());
        assertEquals(16, result.getCharacterCount());
    }
} 