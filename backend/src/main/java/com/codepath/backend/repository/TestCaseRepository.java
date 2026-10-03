package com.codepath.backend.repository;

import com.codepath.backend.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestCaseRepository
        extends JpaRepository<TestCase, Long> {

    List<TestCase> findAllByProblemIdOrderByOrderNumberAsc(Long problemId);

    long countByProblemId(Long problemId);
}