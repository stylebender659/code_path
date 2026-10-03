package com.codepath.backend.repository;

import com.codepath.backend.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    List<Topic> findAllByOrderByOrderNumberAsc();
}