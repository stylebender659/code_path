package com.codepath.backend.repository;

import com.codepath.backend.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository
        extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonId(
            Long userId,
            Long lessonId
    );

    List<LessonProgress> findAllByUserId(Long userId);

    long countByUserIdAndCompletedTrue(Long userId);
}