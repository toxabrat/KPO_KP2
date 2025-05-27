package org.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analysis_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    @Id
    @Column(name = "file_id", nullable = false)
    private String fileId;
    
    @Column(name = "paragraph_count", nullable = false)
    private Integer paragraphCount;
    
    @Column(name = "word_count", nullable = false)
    private Integer wordCount;
    
    @Column(name = "character_count", nullable = false)
    private Integer characterCount;
} 