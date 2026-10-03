package com.codepath.backend.controller;

import com.codepath.backend.model.Lesson;
import com.codepath.backend.model.LessonProgress;
import com.codepath.backend.model.User;
import com.codepath.backend.repository.LessonProgressRepository;
import com.codepath.backend.repository.LessonRepository;
import com.codepath.backend.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/progress")

public class ProgressController {

    private final LessonProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public ProgressController(
            LessonProgressRepository progressRepository,
            UserRepository userRepository,
            LessonRepository lessonRepository
    ) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    @PostMapping("/lessons/{lessonId}/complete")
    public ProgressResponse completeLesson(
            @PathVariable Long lessonId,
            HttpSession session
    ) {

        Long userId = getUserId(session);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() ->
                        new RuntimeException("Lesson not found")
                );

        LessonProgress progress =
                progressRepository
                        .findByUserIdAndLessonId(userId, lessonId)
                        .orElseGet(() ->
                                new LessonProgress(user, lesson)
                        );

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        LessonProgress saved =
                progressRepository.save(progress);

        return new ProgressResponse(
                saved.getLesson().getId(),
                saved.isCompleted(),
                saved.getCompletedAt()
        );
    }

    @GetMapping
    public ProgressSummary getProgress(HttpSession session) {

        Long userId = getUserId(session);

        List<LessonProgress> progress =
                progressRepository.findAllByUserId(userId);

        long completedLessons =
                progress.stream()
                        .filter(LessonProgress::isCompleted)
                        .count();

        long totalLessons = lessonRepository.count();

        double percentage =
                totalLessons == 0
                        ? 0
                        : (completedLessons * 100.0) / totalLessons;

        List<ProgressResponse> lessons =
                progress.stream()
                        .map(item ->
                                new ProgressResponse(
                                        item.getLesson().getId(),
                                        item.isCompleted(),
                                        item.getCompletedAt()
                                )
                        )
                        .toList();

        return new ProgressSummary(
                completedLessons,
                totalLessons,
                percentage,
                lessons
        );
    }

    private Long getUserId(HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        return (Long) userId;
    }

    public record ProgressResponse(
            Long lessonId,
            boolean completed,
            LocalDateTime completedAt
    ) {
    }

    public record ProgressSummary(
            long completedLessons,
            long totalLessons,
            double percentage,
            List<ProgressResponse> lessons
    ) {
    }
}