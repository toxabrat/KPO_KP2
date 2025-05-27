package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.FileDTO;
import org.example.service.FileStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "API для работы с файлами")
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Загрузить файл")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Файл успешно загружен",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FileDTO.class)
            )
        )
    })
    public ResponseEntity<FileDTO> uploadFile(
        @Parameter(description = "Файл для загрузки")
        @RequestParam("file") MultipartFile file
    ) throws IOException {
        FileDTO savedFile = fileStorageService.store(file);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(savedFile);
    }

    @Operation(summary = "Получить список всех файлов")
    @ApiResponse(responseCode = "200", description = "Список файлов успешно получен")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileDTO>> getAllFiles() {
        List<FileDTO> files = fileStorageService.getAllFiles();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok()
            .headers(headers)
            .body(files);
    }

    @Operation(summary = "Получить информацию о файле")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Информация о файле найдена",
                    content = @Content(schema = @Schema(implementation = FileDTO.class))),
        @ApiResponse(responseCode = "404", description = "Файл не найден")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FileDTO> getFile(
            @Parameter(description = "ID файла") @PathVariable Long id) {
        return fileStorageService.getFile(id)
                .map(file -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    return ResponseEntity.ok()
                        .headers(headers)
                        .body(file);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Скачать файл")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Файл успешно скачан",
                    content = @Content(mediaType = "application/octet-stream")),
        @ApiResponse(responseCode = "404", description = "Файл не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> downloadFile(
            @Parameter(description = "ID файла") @PathVariable Long id) {
        try {
            Optional<FileDTO> fileDTO = fileStorageService.getFile(id);
            if (fileDTO.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Optional<byte[]> content = fileStorageService.getFileContent(id);
            if (content.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(fileDTO.get().getContentType()));
            headers.setContentDispositionFormData("attachment", fileDTO.get().getName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(content.get());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Удалить файл")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Файл успешно удален"),
        @ApiResponse(responseCode = "404", description = "Файл не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "ID файла") @PathVariable Long id) {
        try {
            boolean deleted = fileStorageService.deleteFile(id);
            if (deleted) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Проверить существование файла с таким же хешем")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Проверка выполнена успешно"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping(
        value = "/check-exists",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> checkFileExists(
            @Parameter(description = "Файл для проверки") 
            @RequestParam("file") MultipartFile file) {
        try {
            boolean exists = fileStorageService.checkFileExists(file);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.ok()
                .headers(headers)
                .body(exists);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 