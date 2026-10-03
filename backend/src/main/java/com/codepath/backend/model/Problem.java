package com.codepath.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "problems",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_problem_slug",
                        columnNames = "slug"
                )
        }
)
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, unique = true, length = 180)
    private String slug;

    @Column(nullable = false, length = 30)
    private String difficulty;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String examples;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String constraints;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String starterCode;

    @Column(nullable = true, length = 80)
    private String methodName = "";

    @Column(nullable = true, length = 100)
    private String judgeType = "";

    @Column(nullable = false)
    private Integer orderNumber;

    @Column(nullable = false)
    private Boolean published = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Problem() {
        this.createdAt = LocalDateTime.now();
        this.published = true;
    }

    /*
     * Existing constructor.
     *
     * Kept so the current DataInitializer /
     * problem seed code does not break.
     */
    public Problem(
            String title,
            String difficulty,
            String description,
            String examples,
            String constraints,
            String starterCode,
            Integer orderNumber
    ) {
        this.title = title;
        this.slug = createSlug(title);
        this.difficulty = difficulty;
        this.description = description;
        this.examples = examples;
        this.constraints = constraints;
        this.starterCode = starterCode;
        this.orderNumber = orderNumber;

        this.methodName = "";
        this.judgeType = "";
        this.published = true;
        this.createdAt = LocalDateTime.now();
    }

    /*
     * Full constructor for future problem management.
     */
    public Problem(
            String title,
            String slug,
            String difficulty,
            String description,
            String examples,
            String constraints,
            String starterCode,
            String methodName,
            String judgeType,
            Integer orderNumber,
            Boolean published
    ) {
        this.title = title;
        this.slug = slug;
        this.difficulty = difficulty;
        this.description = description;
        this.examples = examples;
        this.constraints = constraints;
        this.starterCode = starterCode;
        this.methodName = methodName;
        this.judgeType = judgeType;
        this.orderNumber = orderNumber;
        this.published = published;
        this.createdAt = LocalDateTime.now();
    }

    private static String createSlug(String title) {

        if (title == null || title.isBlank()) {
            return "";
        }

        return title
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description;
    }

    public String getExamples() {
        return examples;
    }

    public String getConstraints() {
        return constraints;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getJudgeType() {
        return judgeType;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public Boolean getPublished() {
        return published;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setTitle(String title) {
        this.title = title;

        if (this.slug == null || this.slug.isBlank()) {
            this.slug = createSlug(title);
        }
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setJudgeType(String judgeType) {
        this.judgeType = judgeType;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}