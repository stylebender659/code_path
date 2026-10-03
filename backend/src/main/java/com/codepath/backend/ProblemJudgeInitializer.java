package com.codepath.backend;

import com.codepath.backend.model.Problem;
import com.codepath.backend.repository.ProblemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProblemJudgeInitializer implements CommandLineRunner {

    private final ProblemRepository problemRepository;

    public ProblemJudgeInitializer(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Override
    public void run(String... args) {
        Map<String, Problem> problems = problemRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Problem::getTitle,
                        Function.identity(),
                        (a, b) -> a
                ));

        configure(problems, "Two Sum", "twoSum", "INT_ARRAY_INT_TO_INT_ARRAY");
        configure(problems, "Reverse an Array", "reverse", "INT_ARRAY_TO_VOID");
        configure(problems, "Find Maximum Element", "findMax", "INT_ARRAY_TO_INT");
        configure(problems, "Check Palindrome", "isPalindrome", "STRING_TO_BOOLEAN");
        configure(problems, "Valid Parentheses", "isValid", "STRING_TO_BOOLEAN");
        configure(problems, "Reverse Linked List", "reverseList", "LIST_NODE_TO_LIST_NODE");
        configure(problems, "Binary Search", "search", "INT_ARRAY_INT_TO_INT");
        configure(problems, "Maximum Subarray", "maxSubArray", "INT_ARRAY_TO_INT");
        configure(problems, "Merge Intervals", "merge", "INT_MATRIX_TO_INT_MATRIX");
        configure(problems, "Number of Islands", "numIslands", "CHAR_MATRIX_TO_INT");

        System.out.println("CodePath judge metadata initialized.");
    }

    private void configure(
            Map<String, Problem> problems,
            String title,
            String methodName,
            String judgeType
    ) {
        Problem problem = problems.get(title);

        if (problem == null) {
            return;
        }

        boolean changed = false;

        if (!methodName.equals(problem.getMethodName())) {
            problem.setMethodName(methodName);
            changed = true;
        }

        if (!judgeType.equals(problem.getJudgeType())) {
            problem.setJudgeType(judgeType);
            changed = true;
        }

        if (changed) {
            problemRepository.save(problem);
        }
    }
}
