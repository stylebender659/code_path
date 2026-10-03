package com.codepath.backend.repository;

import com.codepath.backend.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findByTitle(String title);

    Optional<Problem> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Problem> findAllByPublishedTrueOrderByOrderNumberAsc();

    List<Problem> findAllByOrderByOrderNumberAsc();
}