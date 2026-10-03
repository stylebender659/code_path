package com.codepath.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "problem_test_cases")
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(nullable = false)
    private Integer orderNumber;

    public TestCase() {}

    public TestCase(
            Problem problem,
            String input,
            String expectedOutput,
            Integer orderNumber
    ) {
        this.problem = problem;
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.orderNumber = orderNumber;
    }

    public Long getId() {
        return id;
    }

    public Problem getProblem() {
        return problem;
    }

    public String getInput() {
        return input;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }
}