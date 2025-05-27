package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.dto.FileAnalysisDTO;
import org.example.service.FileAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@Tag(name = "File Analysis", description = "API для анализа файлов")
public class FileAnalysisController {

    private final FileAnalysisService fileAnalysisService;

    public FileAnalysisController(FileAnalysisService fileAnalysisService) {
        this.fileAnalysisService = fileAnalysisService;
    }

    @PostMapping("/files/{fileId}/analyze")
    @Operation(summary = "Анализировать файл")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Анализ выполнен успешно",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileAnalysisDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Файл не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<FileAnalysisDTO> analyzeFile(
        @Parameter(description = "ID файла") 
        @PathVariable String fileId
    ) {
        try {
            FileAnalysisDTO result = fileAnalysisService.analyzeFile(fileId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 