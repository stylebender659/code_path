package com.codepath.backend.controller;

import com.codepath.backend.model.Lesson;
import com.codepath.backend.repository.LessonRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")

public class LessonController {

    private final LessonRepository lessonRepository;

    public LessonController(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @GetMapping("/topics/{topicId}/lessons")
    public List<Lesson> getLessons(@PathVariable Long topicId) {
        return lessonRepository.findAllByTopicIdOrderByOrderNumberAsc(topicId);
    }

    @GetMapping("/lessons/{id}")
    public Lesson getLesson(@PathVariable Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }
}