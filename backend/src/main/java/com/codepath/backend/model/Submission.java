package com.codepath.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false, length = 20)
    private String language;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String code;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Submission() {}

    public Submission(
            User user,
            Problem problem,
            String language,
            String code
    ) {
        this.user = user;
        this.problem = problem;
        this.language = language;
        this.code = code;
        this.status = "QUEUED";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Problem getProblem() {
        return problem;
    }

    public String getLanguage() {
        return language;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getOutput() {
        return output;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}