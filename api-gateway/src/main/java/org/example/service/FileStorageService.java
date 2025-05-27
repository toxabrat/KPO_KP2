package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.FileDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class FileStorageService {

    private final RestTemplate restTemplate;
    private final String fileStorageUrl;

    public FileStorageService(RestTemplate restTemplate, 
                            @Value("${services.file-storage.url}") String fileStorageUrl) {
        this.restTemplate = restTemplate;
        this.fileStorageUrl = fileStorageUrl;
    }

    public FileDTO uploadFile(MultipartFile file) throws IOException {
        log.debug("Starting file upload to storage service");
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<FileDTO> response = restTemplate.exchange(
            fileStorageUrl + "/api/files",
            HttpMethod.POST,
            requestEntity,
            FileDTO.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload file");
        }

        return response.getBody();
    }
} 