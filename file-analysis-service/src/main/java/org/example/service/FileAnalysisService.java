package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.FileAnalysisDTO;
import org.example.entity.AnalysisResult;
import org.example.repository.AnalysisResultRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Optional;

/**
 * Service for analyzing file contents.
 * Performs text analysis including word count, paragraph count, and character count.
 * Caches analysis results in database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileAnalysisService {
    private final AnalysisResultRepository analysisResultRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${file.storage.url}")
    private String fileStorageUrl;

    public FileAnalysisDTO analyzeFile(String fileId) {
        log.info("Starting analysis for file: {}", fileId);
        
        // Проверяем, есть ли уже результаты анализа
        Optional<AnalysisResult> existingAnalysis = analysisResultRepository.findById(fileId);
        if (existingAnalysis.isPresent()) {
            log.info("Found existing analysis for file: {}", fileId);
            return convertToDTO(existingAnalysis.get());
        }

        // Получаем содержимое файла из file-storage-service
        String fileContent = restTemplate.getForObject(
            fileStorageUrl + "/api/files/" + fileId + "/content",
            String.class
        );

        if (fileContent == null) {
            log.error("Could not retrieve file content for file: {}", fileId);
            throw new RuntimeException("Could not retrieve file content");
        }

        log.info("Retrieved file content, length: {}", fileContent.length());

        // Анализируем содержимое
        int paragraphCount = 0;
        int wordCount = 0;
        int charCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            String line;
            boolean inParagraph = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Подсчет символов (без пробелов)
                int lineCharCount = line.replaceAll("\\s+", "").length();
                charCount += lineCharCount;
                
                // Подсчет слов
                if (!line.isEmpty()) {
                    int lineWordCount = line.split("\\s+").length;
                    wordCount += lineWordCount;
                }
                
                // Подсчет абзацев
                if (line.isEmpty()) {
                    if (inParagraph) {
                        inParagraph = false;
                    }
                } else {
                    if (!inParagraph) {
                        paragraphCount++;
                        inParagraph = true;
                    }
                }
                
                log.debug("Line analysis - chars: {}, words: {}, inParagraph: {}", 
                         lineCharCount, 
                         line.isEmpty() ? 0 : line.split("\\s+").length,
                         inParagraph);
            }
        } catch (Exception e) {
            log.error("Error analyzing text for file: {}", fileId, e);
            throw new RuntimeException("Error analyzing text", e);
        }

        log.info("Analysis complete - paragraphs: {}, words: {}, chars: {}", 
                paragraphCount, wordCount, charCount);

        // Сохраняем результаты анализа
        AnalysisResult analysis = AnalysisResult.builder()
                .fileId(fileId)
                .paragraphCount(paragraphCount)
                .wordCount(wordCount)
                .characterCount(charCount)
                .build();

        log.info("Saving analysis result: {}", analysis);
        AnalysisResult savedAnalysis = analysisResultRepository.save(analysis);
        log.info("Saved analysis result: {}", savedAnalysis);
        
        return convertToDTO(savedAnalysis);
    }

    private FileAnalysisDTO convertToDTO(AnalysisResult entity) {
        FileAnalysisDTO dto = FileAnalysisDTO.builder()
                .fileId(entity.getFileId())
                .paragraphCount(entity.getParagraphCount())
                .wordCount(entity.getWordCount())
                .characterCount(entity.getCharacterCount())
                .build();
        log.info("Converted to DTO: {}", dto);
        return dto;
    }
} 