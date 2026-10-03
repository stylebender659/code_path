# CodePath — Development Rules

## 1. General Rule

CodePath is a real full-stack application, not a static demo.

Every feature should be implemented so that it works with:

- React frontend
- Spring Boot backend
- MySQL database
- REST APIs
- Authenticated user sessions
- Docker-based code execution where required

Do not implement fake frontend-only behavior for features that should be persistent.

---

# 2. Do Not Break Working Features

Before changing existing code:

1. Understand the current implementation.
2. Change only what is necessary.
3. Preserve existing functionality.
4. Run the application after significant changes.
5. Test the affected feature.

Do not replace a large working file with experimental code unless the replacement has been verified.

---

# 3. Frontend Rules

The frontend must communicate with the backend through the API layer.

Use:

```text
frontend/src/api.js