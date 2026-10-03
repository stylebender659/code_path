package com.codepath.backend.controller;

import com.codepath.backend.model.Submission;
import com.codepath.backend.repository.SubmissionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/problems")

public class ProblemProgressController {

    private final SubmissionRepository submissionRepository;

    public ProblemProgressController(
            SubmissionRepository submissionRepository
    ) {
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/solved")
    public List<Long> getSolvedProblemIds(
            HttpSession session
    ) {
        Object userIdAttribute = session.getAttribute("userId");

        if (userIdAttribute == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        Long userId = (Long) userIdAttribute;

        return submissionRepository
                .findByUserIdAndStatus(userId, "ACCEPTED")
                .stream()
                .map(Submission::getProblem)
                .filter(problem -> problem != null)
                .map(problem -> problem.getId())
                .distinct()
                .toList();
    }
}
