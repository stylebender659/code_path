# CodePath

CodePath is a full-stack DSA learning and coding platform that combines structured learning, coding problems, Java code execution, automated judging, and progress tracking.

---

## Features

- User registration and login
- Session-based authentication
- Structured DSA roadmap
- Topics and lessons
- Lesson completion tracking
- Personal dashboard
- Coding problem catalogue
- Java code editor
- Run Code
- Automated submissions
- Docker-isolated code execution
- Test-case based judging
- Submission history
- Solved problem tracking
- Progress tracking

---

## Technology Stack

### Frontend

- React
- Vite
- JavaScript
- Bootstrap
- CSS

### Backend

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- Maven

### Database

- MySQL

### Code Execution

- Docker
- Eclipse Temurin Java 21

---

## Architecture

```text
                 ┌───────────────┐
                 │     User      │
                 │   Browser     │
                 └───────┬───────┘
                         │
                         ▼
                 ┌───────────────┐
                 │ React + Vite  │
                 │   Frontend    │
                 └───────┬───────┘
                         │
                      REST API
                         │
                         ▼
                 ┌───────────────┐
                 │ Spring Boot   │
                 │   Backend     │
                 └───────┬───────┘
                         │
              ┌──────────┴──────────┐
              │                     │
              ▼                     ▼
       ┌─────────────┐       ┌──────────────┐
       │    MySQL    │       │    Docker    │
       │   Database  │       │ Java Judge   │
       └─────────────┘       └──────────────┘