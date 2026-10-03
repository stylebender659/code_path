package com.codepath.backend.controller;

import com.codepath.backend.model.Problem;
import com.codepath.backend.model.Submission;
import com.codepath.backend.model.TestCase;
import com.codepath.backend.model.User;
import com.codepath.backend.repository.ProblemRepository;
import com.codepath.backend.repository.SubmissionRepository;
import com.codepath.backend.repository.TestCaseRepository;
import com.codepath.backend.repository.UserRepository;
import com.codepath.backend.service.CodeExecutionService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")

public class SubmissionController {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodeExecutionService codeExecutionService;

    public SubmissionController(
            SubmissionRepository submissionRepository,
            UserRepository userRepository,
            ProblemRepository problemRepository,
            TestCaseRepository testCaseRepository,
            CodeExecutionService codeExecutionService
    ) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.testCaseRepository = testCaseRepository;
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(
            @Valid @RequestBody CreateSubmissionRequest request,
            HttpSession session
    ) {
        Long userId = requireUser(session);

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        if (request.problemId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Problem ID is required"
            );
        }

        Problem problem = problemRepository
                .findById(request.problemId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Problem not found"
                        )
                );

        if (!request.language().equalsIgnoreCase("java")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only Java is supported currently"
            );
        }

        if (request.code().length() > 50000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Code is too large"
            );
        }

        List<TestCase> testCases =
                testCaseRepository
                        .findAllByProblemIdOrderByOrderNumberAsc(
                                problem.getId()
                        );

        if (testCases.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No test cases are configured for this problem"
            );
        }

        if (problem.getMethodName() == null
                || problem.getMethodName().isBlank()
                || problem.getJudgeType() == null
                || problem.getJudgeType().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "This problem is not configured for code execution yet"
            );
        }

        Submission submission =
                submissionRepository.save(
                        new Submission(
                                user,
                                problem,
                                request.language().toLowerCase(),
                                request.code()
                        )
                );

        String finalStatus = "ACCEPTED";

        StringBuilder finalOutput =
                new StringBuilder();

        int passedTests = 0;

        for (int i = 0; i < testCases.size(); i++) {

            TestCase testCase = testCases.get(i);

            CodeExecutionService.ExecutionResult result =
                    codeExecutionService.executeProblem(
                            problem,
                            request.code(),
                            testCase.getInput()
                    );

            String actualOutput =
                    normalizeOutput(result.output());

            String expectedOutput =
                    normalizeOutput(testCase.getExpectedOutput());

            finalOutput
                    .append("Test Case ")
                    .append(i + 1)
                    .append("\n");

            finalOutput
                    .append("Input: ")
                    .append(testCase.getInput())
                    .append("\n");

            finalOutput
                    .append("Expected: ")
                    .append(expectedOutput)
                    .append("\n");

            finalOutput
                    .append("Actual: ")
                    .append(actualOutput)
                    .append("\n");

            if ("COMPILATION_ERROR".equals(result.status())) {
                finalStatus = "COMPILATION_ERROR";

                finalOutput
                        .append("Status: COMPILATION_ERROR\n\n");

                break;
            }

            if ("TIME_LIMIT_EXCEEDED".equals(result.status())) {
                finalStatus = "TIME_LIMIT_EXCEEDED";

                finalOutput
                        .append("Status: TIME_LIMIT_EXCEEDED\n\n");

                break;
            }

            if ("SYSTEM_ERROR".equals(result.status())) {
                finalStatus = "SYSTEM_ERROR";

                finalOutput
                        .append("Status: SYSTEM_ERROR\n\n");

                break;
            }

            if (!"SUCCESS".equals(result.status())) {
                finalStatus = "RUNTIME_ERROR";

                finalOutput
                        .append("Status: RUNTIME_ERROR\n\n");

                break;
            }

            if (!actualOutput.equals(expectedOutput)) {
                finalStatus = "WRONG_ANSWER";

                finalOutput
                        .append("Status: WRONG_ANSWER\n\n");

                break;
            }

            passedTests++;

            finalOutput
                    .append("Status: PASSED\n\n");
        }

        if ("ACCEPTED".equals(finalStatus)) {
            finalOutput
                    .append("Accepted\n")
                    .append("Passed ")
                    .append(passedTests)
                    .append("/")
                    .append(testCases.size())
                    .append(" test cases.");
        } else {
            finalOutput
                    .append("Passed ")
                    .append(passedTests)
                    .append("/")
                    .append(testCases.size())
                    .append(" test cases.");
        }

        submission.setStatus(finalStatus);
        submission.setOutput(
                finalOutput.toString().trim()
        );

        submission =
                submissionRepository.save(submission);

        return toResponse(submission);
    }

    @GetMapping
    public List<SubmissionResponse> mine(
            HttpSession session
    ) {
        Long userId = requireUser(session);

        return submissionRepository
                .findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Long requireUser(HttpSession session) {
        Object userId =
                session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        return (Long) userId;
    }

    private String normalizeOutput(String output) {
        if (output == null) {
            return "";
        }

        return output
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();
    }

    private SubmissionResponse toResponse(
            Submission submission
    ) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getProblem().getId(),
                submission.getProblem().getTitle(),
                submission.getLanguage(),
                submission.getStatus(),
                submission.getOutput(),
                submission.getCreatedAt()
        );
    }

    public record CreateSubmissionRequest(
            @NotBlank String language,
            @NotBlank String code,
            Long problemId
    ) {
    }

    public record SubmissionResponse(
            Long id,
            Long problemId,
            String problemTitle,
            String language,
            String status,
            String output,
            LocalDateTime createdAt
    ) {
    }
}
