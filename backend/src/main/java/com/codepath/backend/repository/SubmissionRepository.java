package com.codepath.backend.repository;

import com.codepath.backend.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
    List<Submission> findByUserIdAndStatus(Long userId, String status);

    long countByUserIdAndStatus(Long userId, String status);
}