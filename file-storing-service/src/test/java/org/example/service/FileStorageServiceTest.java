package org.example.service;

import org.example.dto.FileDTO;
import org.example.entity.FileEntity;
import org.example.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private FileRepository fileRepository;
    private MockMultipartFile testFile;
    private FileEntity testFileEntity;
    private String testContent = "test content";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileRepository = mock(FileRepository.class);
        fileStorageService = new FileStorageService(fileRepository);
        try {
            var field = FileStorageService.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            field.set(fileStorageService, tempDir.toString());
        } catch (Exception e) {
            fail("Failed to start tests", e);
        }
        fileStorageService.init();

        testFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            testContent.getBytes()
        );

        testFileEntity = FileEntity.builder()
            .id(1L)
            .name("test.txt")
            .location(tempDir.resolve("test.txt").toString())
            .contentType("text/plain")
            .size((long) testContent.getBytes().length)
            .hash(DigestUtils.md5DigestAsHex(testContent.getBytes()))
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    void storeNewFileSuccess() throws IOException {
        when(fileRepository.findByHash(any())).thenReturn(Optional.empty());
        when(fileRepository.save(any())).thenReturn(testFileEntity);
        FileDTO result = fileStorageService.store(testFile);

        assertNotNull(result);
        assertEquals(testFileEntity.getName(), result.getName());
        assertEquals(testFileEntity.getContentType(), result.getContentType());
        assertEquals(testFileEntity.getSize(), result.getSize());
        assertEquals(testFileEntity.getHash(), result.getHash());

        verify(fileRepository).findByHash(any());
        verify(fileRepository).save(any());
    }

    @Test
    void storeExistingFileReturnsExisting() throws IOException {
        when(fileRepository.findByHash(any())).thenReturn(Optional.of(testFileEntity));
        FileDTO result = fileStorageService.store(testFile);

        assertNotNull(result);
        assertEquals(testFileEntity.getName(), result.getName());
        
        verify(fileRepository).findByHash(any());
        verify(fileRepository, never()).save(any());
    }

    @Test
    void getAllFilesSuccess() {
        when(fileRepository.findAll()).thenReturn(Arrays.asList(testFileEntity));

        List<FileDTO> results = fileStorageService.getAllFiles();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(testFileEntity.getName(), results.get(0).getName());
        
        verify(fileRepository).findAll();
    }

    @Test
    void getFileContentExistsSuccess() throws IOException {
        Path filePath = tempDir.resolve("test.txt");
        Files.write(filePath, testContent.getBytes());
        FileEntity fileWithRealPath = testFileEntity;
        fileWithRealPath.setLocation(filePath.toString());
        when(fileRepository.findById(1L)).thenReturn(Optional.of(fileWithRealPath));
        Optional<byte[]> result = fileStorageService.getFileContent(1L);

        assertTrue(result.isPresent());
        assertArrayEquals(testContent.getBytes(), result.get());
        
        verify(fileRepository).findById(1L);
    }

    @Test
    void checkFileExistsTrue() throws IOException {
        when(fileRepository.existsByHash(any())).thenReturn(true);

        boolean result = fileStorageService.checkFileExists(testFile);
        assertTrue(result);
        
        verify(fileRepository).existsByHash(any());
    }

} 