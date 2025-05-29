package org.example.repository;

import org.example.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, String> {
} 