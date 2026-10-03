# CodePath — Project Memory

> Last updated: October 2026
>
> This file stores the important technical decisions, current project state,
> completed work, known issues, and deployment requirements for CodePath.

---

# 1. Project Overview

CodePath is a full-stack DSA learning and coding-practice platform inspired by
platforms such as LeetCode and TakeUForward.

The goal is to provide:

- DSA learning roadmap
- Topic-based lessons
- Lesson progress tracking
- Coding problems
- Problem solving workspace
- Java code execution
- Test-case execution
- Code submission and judging
- Accepted / wrong / compilation / runtime results
- Solved problem tracking
- Submission history
- Dashboard
- Authentication
- Session-based security
- Database-backed content
- Production deployment

The platform should behave like a real developer-built product rather than
a static college project.

---

# 2. Project Location

Local project:

C:\Users\Ashwin\OneDrive\Desktop\CodePath\

Structure:

CodePath/
├── backend/
├── frontend/
├── database/
├── Architecture.md
├── PRD.md
├── README.md
├── design.md
├── rules.md
├── tasks.md
├── memory.md
└── .gitignore

---

# 3. Frontend

Technology:

- React 19
- Vite
- JavaScript
- Bootstrap
- Axios dependency
- React Router
- CSS

Important frontend files:

frontend/src/
├── App.jsx
├── api.js
├── index.css
└── main.jsx

main.jsx is working and should not be changed unnecessarily.

Current API base:

```js
const API_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8080/api";