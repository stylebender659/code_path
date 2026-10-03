package com.codepath.backend.controller;

import com.codepath.backend.model.Lesson;
import com.codepath.backend.model.LessonProgress;
import com.codepath.backend.model.Topic;
import com.codepath.backend.repository.LessonProgressRepository;
import com.codepath.backend.repository.LessonRepository;
import com.codepath.backend.repository.ProblemRepository;
import com.codepath.backend.repository.SubmissionRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")

public class DashboardController {

    private final LessonRepository lessonRepository;
    private final LessonProgressRepository progressRepository;
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;

    public DashboardController(
            LessonRepository lessonRepository,
            LessonProgressRepository progressRepository,
            SubmissionRepository submissionRepository,
            ProblemRepository problemRepository
    ) {
        this.lessonRepository = lessonRepository;
        this.progressRepository = progressRepository;
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
    }

    @GetMapping
    public DashboardResponse getDashboard(HttpSession session) {

        Long userId = getUserId(session);

        // ---------------------------------------------------------
        // LESSON PROGRESS
        // ---------------------------------------------------------

        List<Lesson> allLessons =
                lessonRepository.findAll();

        List<LessonProgress> userProgress =
                progressRepository.findAllByUserId(userId);

        Set<Long> completedLessonIds =
                userProgress.stream()
                        .filter(LessonProgress::isCompleted)
                        .map(progress ->
                                progress.getLesson().getId()
                        )
                        .collect(Collectors.toSet());

        long completedLessons =
                completedLessonIds.size();

        long totalLessons =
                allLessons.size();

        double overallProgress =
                totalLessons == 0
                        ? 0
                        : (completedLessons * 100.0) / totalLessons;


        // ---------------------------------------------------------
        // PROBLEM PROGRESS
        // ---------------------------------------------------------

        long problemsSolved =
                submissionRepository
                        .countByUserIdAndStatus(
                                userId,
                                "ACCEPTED"
                        );

        /*
         * The same problem may be submitted successfully more
         * than once. Count distinct solved problems instead.
         */
        long distinctProblemsSolved =
                submissionRepository
                        .findByUserIdAndStatus(
                                userId,
                                "ACCEPTED"
                        )
                        .stream()
                        .filter(submission ->
                                submission.getProblem() != null
                        )
                        .map(submission ->
                                submission.getProblem().getId()
                        )
                        .distinct()
                        .count();

        long totalProblems =
                problemRepository.count();


        // ---------------------------------------------------------
        // TOPIC PROGRESS
        // ---------------------------------------------------------

        Map<Topic, List<Lesson>> lessonsByTopic =
                allLessons.stream()
                        .filter(lesson ->
                                lesson.getTopic() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        Lesson::getTopic,
                                        LinkedHashMap::new,
                                        Collectors.toList()
                                )
                        );

        List<TopicProgress> topics =
                lessonsByTopic.entrySet()
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        entry ->
                                                entry.getKey()
                                                        .getOrderNumber()
                                )
                        )
                        .map(entry -> {

                            Topic topic =
                                    entry.getKey();

                            List<Lesson> topicLessons =
                                    entry.getValue();

                            long completed =
                                    topicLessons.stream()
                                            .filter(lesson ->
                                                    completedLessonIds
                                                            .contains(
                                                                    lesson.getId()
                                                            )
                                            )
                                            .count();

                            double progress =
                                    topicLessons.isEmpty()
                                            ? 0
                                            : (completed * 100.0)
                                            / topicLessons.size();

                            return new TopicProgress(
                                    topic.getTitle(),
                                    progress
                            );
                        })
                        .toList();


        // ---------------------------------------------------------
        // CURRENT STREAK
        // ---------------------------------------------------------

        int currentStreak =
                calculateCurrentStreak(
                        userProgress
                );


        // ---------------------------------------------------------
        // RESPONSE
        // ---------------------------------------------------------

        return new DashboardResponse(
                overallProgress,
                completedLessons,
                totalLessons,
                distinctProblemsSolved,
                totalProblems,
                currentStreak,
                topics
        );
    }


    // =============================================================
    // CURRENT STREAK
    // =============================================================

    private int calculateCurrentStreak(
            List<LessonProgress> progress
    ) {

        Set<LocalDate> activityDates =
                progress.stream()
                        .filter(LessonProgress::isCompleted)
                        .map(LessonProgress::getCompletedAt)
                        .filter(Objects::nonNull)
                        .map(LocalDateTime::toLocalDate)
                        .collect(Collectors.toSet());

        if (activityDates.isEmpty()) {
            return 0;
        }

        LocalDate today =
                LocalDate.now(
                        ZoneId.systemDefault()
                );

        /*
         * If the user has not done anything today,
         * allow the streak to continue from yesterday.
         */
        LocalDate currentDate;

        if (activityDates.contains(today)) {
            currentDate = today;
        } else if (activityDates.contains(today.minusDays(1))) {
            currentDate = today.minusDays(1);
        } else {
            return 0;
        }

        int streak = 0;

        while (activityDates.contains(currentDate)) {
            streak++;
            currentDate = currentDate.minusDays(1);
        }

        return streak;
    }


    // =============================================================
    // SESSION
    // =============================================================

    private Long getUserId(HttpSession session) {

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


    // =============================================================
    // RESPONSE RECORDS
    // =============================================================

    public record DashboardResponse(
            double overallProgress,
            long completedLessons,
            long totalLessons,
            long problemsSolved,
            long totalProblems,
            int currentStreak,
            List<TopicProgress> topics
    ) {
    }


    public record TopicProgress(
            String name,
            double progress
    ) {
    }
}