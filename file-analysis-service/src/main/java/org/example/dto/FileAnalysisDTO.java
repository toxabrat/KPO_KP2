package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileAnalysisDTO {
    private String fileId;
    private Integer paragraphCount;
    private Integer wordCount;
    private Integer characterCount;
} 