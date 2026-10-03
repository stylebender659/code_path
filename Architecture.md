# CodePath — System Architecture

## 1. Overview

CodePath is a full-stack web application consisting of:

- React frontend
- Spring Boot backend
- MySQL database
- Docker-based Java code execution environment

The system follows a client-server architecture with a REST API between the frontend and backend.

```text
┌───────────────────────────────┐
│           User                │
│        Web Browser            │
└───────────────┬───────────────┘
                │
                │ HTTP / HTTPS
                ▼
┌───────────────────────────────┐
│        React Frontend         │
│                               │
│  UI / Pages / State / API     │
└───────────────┬───────────────┘
                │
                │ REST API
                ▼
┌───────────────────────────────┐
│       Spring Boot API         │
│                               │
│ Controllers                   │
│      ↓                        │
│ Services                      │
│      ↓                        │
│ Repositories                  │
└───────────────┬───────────────┘
                │
        ┌───────┴────────┐
        │                │
        ▼                ▼
┌──────────────┐  ┌─────────────────┐
│    MySQL     │  │ Docker Execution│
│   Database   │  │     Sandbox     │
└──────────────┘  └─────────────────┘