# CodePath — Product Requirements Document

## 1. Product Overview

**Product Name:** CodePath

CodePath is a full-stack web platform designed to help developers learn Data Structures and Algorithms (DSA) through structured lessons, topic-based learning, coding problems, progress tracking, and an integrated online code execution and submission system.

The platform combines guided learning with practical problem solving.

The goal is to provide a single platform where a learner can:

- Learn DSA concepts in a structured order.
- Study individual lessons.
- Track completed lessons.
- Practice coding problems.
- Execute Java solutions against test cases.
- Submit solutions to an automated judge.
- View submission history.
- Track solved problems.
- Monitor learning progress through a dashboard.

---

# 2. Problem Statement

Many DSA learners use multiple disconnected resources:

- One platform for theory.
- Another platform for coding problems.
- Another system for tracking progress.
- Separate tools for running code.
- Personal notes or spreadsheets for monitoring completion.

This creates unnecessary friction and makes it difficult for learners to understand their overall learning progress.

CodePath aims to combine these core learning activities into one platform.

---

# 3. Product Goals

## Primary Goals

1. Provide structured DSA learning.
2. Provide interactive coding practice.
3. Execute submitted Java code securely.
4. Automatically evaluate solutions against test cases.
5. Track learner progress.
6. Provide a useful personal dashboard.
7. Maintain user authentication and session-based access control.
8. Provide a scalable backend architecture where learning content and problems are database-driven.
9. Provide a professional developer-oriented user experience.
10. Prepare the platform for public deployment.

---

# 4. Target Users

## Primary Users

Students and beginner-to-intermediate developers who want to:

- Learn DSA.
- Practice programming problems.
- Prepare for software-development interviews.
- Track their learning progress.
- Practice Java coding.

## Secondary Users

Future administrators or content maintainers who may manage:

- Topics.
- Lessons.
- Problems.
- Test cases.
- Learning content.

---

# 5. Core Features

## 5.1 User Registration

Users can create an account using:

- Name
- Email
- Password

Passwords are stored securely using password hashing.

---

## 5.2 User Login

Registered users can log in using:

- Email
- Password

Authentication is maintained using an HTTP session.

---

## 5.3 User Logout

Authenticated users can log out.

The server invalidates the active session.

---

# 6. Learning System

## 6.1 Topics

DSA content is organized into topics.

Examples include:

- Arrays
- Strings
- Linked Lists
- Searching
- Sorting
- Trees
- Dynamic Programming
- Graphs

The exact topic list is maintained by the backend/database.

---

## 6.2 Lessons

Each topic can contain multiple lessons.

Lessons provide structured learning material before or alongside coding practice.

Users can mark lessons as completed.

---

## 6.3 Lesson Progress

The platform records lesson completion for authenticated users.

The dashboard can use this information to calculate learning progress.

---

# 7. Coding Problem System

CodePath provides a database-driven coding problem system.

Each problem can contain information such as:

- Problem ID
- Title
- Slug
- Description
- Difficulty
- Examples
- Constraints
- Starter code
- Judge type
- Method name
- Published status
- Ordering information

The problem data is stored in MySQL.

The frontend retrieves problems through REST APIs.

### Current seeded problems

The initial platform contains 10 problems:

1. Two Sum
2. Reverse an Array
3. Find Maximum Element
4. Check Palindrome
5. Valid Parentheses
6. Reverse Linked List
7. Binary Search
8. Maximum Subarray
9. Merge Intervals
10. Number of Islands

The architecture is designed so that problems are primarily data-driven rather than requiring a frontend rewrite for every new problem.

---

# 8. Code Execution

CodePath provides an online Java code execution system.

The execution service:

1. Receives the submitted source code.
2. Validates the request.
3. Generates the required execution harness.
4. Creates a temporary execution environment.
5. Compiles the Java source.
6. Executes the program.
7. Captures output.
8. Applies execution limits.
9. Returns the execution result.
10. Cleans up the temporary environment.

---

# 9. Secure Code Execution

User-submitted code is untrusted.

Code execution therefore uses Docker isolation.

The execution environment applies restrictions including:

- Network disabled.
- Memory limit.
- CPU limit.
- Process limit.
- Dropped Linux capabilities.
- `no-new-privileges`.
- Read-only filesystem.
- Temporary filesystem for runtime operations.
- Execution timeout.
- Output-size limit.

The execution system also handles:

- Compilation errors.
- Runtime errors.
- Time-limit exceeded.
- Excessive output.
- System errors.

---

# 10. Submission and Judging System

Users can submit a solution for a coding problem.

The submission system:

1. Receives the problem ID.
2. Receives the submitted code.
3. Receives the selected language.
4. Loads the problem's test cases.
5. Executes the code against each test case.
6. Compares actual output with expected output.
7. Determines the final submission status.
8. Stores the submission in the database.

Possible statuses include:

- `ACCEPTED`
- `WRONG_ANSWER`
- `COMPILATION_ERROR`
- `RUNTIME_ERROR`
- `TIME_LIMIT_EXCEEDED`
- `SYSTEM_ERROR`

---

# 11. Solved Problems

A problem is considered solved when the user has an accepted submission for that problem.

The platform can retrieve the user's solved problem IDs.

Solved problems are reflected in:

- Problem listings.
- Dashboard statistics.
- Progress information.

---

# 12. Submission History

Authenticated users can view their recent submissions.

Submission records contain information such as:

- Submission ID
- Problem
- Language
- Status
- Output/result
- Creation timestamp

---

# 13. Dashboard

The dashboard provides an overview of the learner's activity.

It can display:

- Overall learning progress.
- Lessons completed.
- Problems solved.
- Current streak.
- User information.
- Topic progress.
- Recent submissions.

The dashboard obtains its information from backend APIs rather than relying only on browser-local state.

---

# 14. Authentication and Authorization

CodePath uses session-based authentication.

The server stores the authenticated user's ID in the HTTP session.

Protected resources verify the presence of the authenticated user session.

Examples of protected functionality include:

- User progress.
- Lesson completion.
- Solved problems.
- Dashboard.
- Submission history.
- Problem submission.

Unauthenticated requests to protected endpoints return an unauthorized response.

---

# 15. Backend Architecture

The backend is implemented using:

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Maven

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database