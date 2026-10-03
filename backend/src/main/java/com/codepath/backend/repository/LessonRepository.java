package com.codepath.backend.repository;

import com.codepath.backend.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findAllByTopicIdOrderByOrderNumberAsc(Long topicId);
}