package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.FileAnalysisDTO;
import org.example.service.FileAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Tag(name = "File Analysis", description = "API для анализа файлов")
public class FileAnalysisController {
    private final FileAnalysisService fileAnalysisService;

    @Operation(summary = "Анализировать файл")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Анализ выполнен успешно",
                    content = @Content(schema = @Schema(implementation = FileAnalysisDTO.class))),
        @ApiResponse(responseCode = "404", description = "Файл не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/files/{fileId}/analyze")
    public ResponseEntity<FileAnalysisDTO> analyzeFile(
            @Parameter(description = "ID файла") @PathVariable String fileId) {
        try {
            FileAnalysisDTO analysis = fileAnalysisService.analyzeFile(fileId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 