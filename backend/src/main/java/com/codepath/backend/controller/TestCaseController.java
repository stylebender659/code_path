package com.codepath.backend.controller;

import com.codepath.backend.repository.TestCaseRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")

public class TestCaseController {

    private final TestCaseRepository testCaseRepository;

    public TestCaseController(
            TestCaseRepository testCaseRepository
    ) {
        this.testCaseRepository = testCaseRepository;
    }

    @GetMapping("/{problemId}/test-cases")
    public List<TestCaseResponse> getTestCases(
            @PathVariable Long problemId
    ) {

        return testCaseRepository
                .findAllByProblemIdOrderByOrderNumberAsc(problemId)
                .stream()
                .map(testCase ->
                        new TestCaseResponse(
                                testCase.getId(),
                                testCase.getInput(),
                                testCase.getExpectedOutput(),
                                testCase.getOrderNumber()
                        )
                )
                .toList();
    }

    public record TestCaseResponse(
            Long id,
            String input,
            String expectedOutput,
            Integer orderNumber
    ) {}
}