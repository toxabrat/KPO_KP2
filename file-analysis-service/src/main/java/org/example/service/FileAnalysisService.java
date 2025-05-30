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
        Optional<AnalysisResult> existingAnalysis = analysisResultRepository.findById(fileId);
        if (existingAnalysis.isPresent()) {
            return convertToDTO(existingAnalysis.get());
        }

        String fileContent = restTemplate.getForObject(
            fileStorageUrl + "/api/files/" + fileId + "/content",
            String.class
        );
        if (fileContent == null) {
            throw new RuntimeException("retrieve file content");
        }
        int paragraphCount = 0;
        int wordCount = 0;
        int charCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new StringReader(fileContent))) {
            String line;
            boolean inParagraph = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                int lineCharCount = line.replaceAll("\\s+", "").length();
                charCount += lineCharCount;
                
                if (!line.isEmpty()) {
                    int lineWordCount = line.split("\\s+").length;
                    wordCount += lineWordCount;
                }
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
                
            }
        } catch (Exception e) {
            throw new RuntimeException("Error analyzing text", e);
        }

        AnalysisResult analysis = AnalysisResult.builder()
                .fileId(fileId)
                .paragraphCount(paragraphCount)
                .wordCount(wordCount)
                .characterCount(charCount)
                .build();

        AnalysisResult savedAnalysis = analysisResultRepository.save(analysis);
        return convertToDTO(savedAnalysis);
    }

    private FileAnalysisDTO convertToDTO(AnalysisResult entity) {
        FileAnalysisDTO dto = FileAnalysisDTO.builder()
                .fileId(entity.getFileId())
                .paragraphCount(entity.getParagraphCount())
                .wordCount(entity.getWordCount())
                .characterCount(entity.getCharacterCount())
                .build();
        return dto;
    }
} 