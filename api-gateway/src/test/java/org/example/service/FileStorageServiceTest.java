package org.example.service;

import org.example.dto.FileDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FileStorageService fileStorageService;

    private static final String FILE_STORAGE_URL = "http://file-storing-service:8080";
    private MockMultipartFile testFile;
    private FileDTO expectedFileDTO;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileStorageService, "fileStorageUrl", FILE_STORAGE_URL);
        
        testFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        expectedFileDTO = FileDTO.builder()
            .id(1L)
            .name("test.txt")
            .contentType("text/plain")
            .size(12L)
            .hash("testhash")
            .build();
    }

    @Test
    void uploadFileSuccess() throws Exception {
        when(restTemplate.exchange(
            eq(FILE_STORAGE_URL + "/api/files"),
            any(),
            any(),
            eq(FileDTO.class)
        )).thenReturn(new ResponseEntity<>(expectedFileDTO, HttpStatus.OK));

        FileDTO result = fileStorageService.uploadFile(testFile);

        assertNotNull(result);
        assertEquals(expectedFileDTO.getId(), result.getId());
        assertEquals(expectedFileDTO.getName(), result.getName());
        assertEquals(expectedFileDTO.getContentType(), result.getContentType());
        assertEquals(expectedFileDTO.getSize(), result.getSize());
        assertEquals(expectedFileDTO.getHash(), result.getHash());
    }
} 