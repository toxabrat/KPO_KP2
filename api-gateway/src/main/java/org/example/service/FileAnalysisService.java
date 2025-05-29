package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.FileAnalysisDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Gateway service for file analysis operations.
 * Routes analysis requests to the file-analysis-service.
 */
@Slf4j
@Service
public class FileAnalysisService {

    private final RestTemplate restTemplate;
    private final String fileAnalysisUrl;

    public FileAnalysisService(RestTemplate restTemplate,
                             @Value("${services.file-analysis.url}") String fileAnalysisUrl) {
        this.restTemplate = restTemplate;
        this.fileAnalysisUrl = fileAnalysisUrl;
    }

    public FileAnalysisDTO analyzeFile(String fileId) {
        String url = fileAnalysisUrl + "/api/analysis/files/" + fileId + "/analyze";
        

        ResponseEntity<FileAnalysisDTO> response = restTemplate.postForEntity(
            url,
            null,
            FileAnalysisDTO.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to analyze file");
        }

        return response.getBody();
    }
}