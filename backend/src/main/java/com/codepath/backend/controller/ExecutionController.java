package com.codepath.backend.controller;

import com.codepath.backend.model.Problem;
import com.codepath.backend.repository.ProblemRepository;
import com.codepath.backend.service.CodeExecutionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/execution")

public class ExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final ProblemRepository problemRepository;

    public ExecutionController(
            CodeExecutionService codeExecutionService,
            ProblemRepository problemRepository
    ) {
        this.codeExecutionService = codeExecutionService;
        this.problemRepository = problemRepository;
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.OK)
    public ExecutionResponse run(
            @Valid @RequestBody RunRequest request
    ) {
        Problem problem = problemRepository
                .findById(request.problemId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Problem not found"
                        )
                );

        CodeExecutionService.ExecutionResult result =
                codeExecutionService.executeProblem(
                        problem,
                        request.code(),
                        request.input()
                );

        return new ExecutionResponse(
                result.status(),
                result.output()
        );
    }

    public record RunRequest(
            @NotNull Long problemId,
            @NotBlank String code,
            String input
    ) {
    }

    public record ExecutionResponse(
            String status,
            String output
    ) {
    }
}
