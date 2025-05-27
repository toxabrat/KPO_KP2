package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.FileAnalysisDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

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
        log.debug("Starting file analysis request for file ID: {}", fileId);
        String url = fileAnalysisUrl + "/api/analysis/files/" + fileId + "/analyze";
        log.debug("Making request to: {}", url);
        
        try {
            ResponseEntity<FileAnalysisDTO> response = restTemplate.postForEntity(
                url,
                null,
                FileAnalysisDTO.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("Failed to analyze file. Status: {}, Body: {}", 
                         response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to analyze file: Unexpected response");
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Client error while analyzing file: {}", e.getMessage());
            throw new RuntimeException("File not found or invalid request: " + e.getMessage());
        } catch (HttpServerErrorException e) {
            log.error("Server error while analyzing file: {}", e.getMessage());
            throw new RuntimeException("Analysis service error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while analyzing file: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze file: " + e.getMessage());
        }
    }
}