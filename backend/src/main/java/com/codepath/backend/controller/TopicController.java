package com.codepath.backend.controller;

import com.codepath.backend.model.Topic;
import com.codepath.backend.repository.TopicRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicRepository topicRepository;

    public TopicController(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    @GetMapping
    public List<Topic> getTopics() {
        return topicRepository.findAllByOrderByOrderNumberAsc();
    }
}