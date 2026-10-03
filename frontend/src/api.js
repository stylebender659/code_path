const API_URL =
  import.meta.env.VITE_API_URL || "http://localhost:8080/api";

/* =========================================================
   RESPONSE HELPER
========================================================= */

async function readResponse(response, fallbackMessage) {
  const text = await response.text();

  let data = {};

  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = {};
  }

  if (!response.ok) {
    const message =
      data.message ||
      data.detail ||
      data.error ||
      `Request failed (${response.status})`;

    throw new Error(message);
  }

  return data;
}

/* =========================================================
   AUTH
========================================================= */

export async function registerUser(name, email, password) {
  const response = await fetch(`${API_URL}/auth/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify({
      name,
      email,
      password,
    }),
  });

  return readResponse(response, "Registration failed");
}

export async function loginUser(email, password) {
  const response = await fetch(`${API_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify({
      email,
      password,
    }),
  });

  return readResponse(response, "Login failed");
}

export async function logoutUser() {
  const response = await fetch(`${API_URL}/auth/logout`, {
    method: "POST",
    credentials: "include",
  });

  return readResponse(response, "Logout failed");
}

export async function getCurrentUser() {
  const response = await fetch(`${API_URL}/auth/me`, {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    return null;
  }

  return response.json();
}

/* =========================================================
   TOPICS
========================================================= */

export async function getTopics() {
  const response = await fetch(`${API_URL}/topics`, {
    method: "GET",
    credentials: "include",
  });

  return readResponse(response, "Failed to load topics");
}

/* =========================================================
   LESSONS
========================================================= */

export async function getLessons(topicId) {
  const response = await fetch(
    `${API_URL}/topics/${topicId}/lessons`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  return readResponse(response, "Failed to load lessons");
}

export async function completeLesson(lessonId) {
  const response = await fetch(
    `${API_URL}/progress/lessons/${lessonId}/complete`,
    {
      method: "POST",
      credentials: "include",
    }
  );

  return readResponse(
    response,
    "Failed to complete lesson"
  );
}

/* =========================================================
   PROGRESS
========================================================= */

export async function getProgress() {
  const response = await fetch(`${API_URL}/progress`, {
    method: "GET",
    credentials: "include",
  });

  return readResponse(
    response,
    "Failed to load progress"
  );
}

/* =========================================================
   DASHBOARD
========================================================= */

export async function getDashboard() {
  const response = await fetch(`${API_URL}/dashboard`, {
    method: "GET",
    credentials: "include",
  });

  return readResponse(
    response,
    "Failed to load dashboard"
  );
}

/* =========================================================
   PROBLEMS
========================================================= */

export async function getProblems() {
  const response = await fetch(`${API_URL}/problems`, {
    method: "GET",
    credentials: "include",
  });

  return readResponse(
    response,
    "Failed to load problems"
  );
}

export async function getProblem(id) {
  const response = await fetch(
    `${API_URL}/problems/${id}`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  return readResponse(
    response,
    "Failed to load problem"
  );
}

/* =========================================================
   SOLVED PROBLEMS
========================================================= */

export async function getSolvedProblemIds() {
  const response = await fetch(
    `${API_URL}/problems/solved`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  return readResponse(
    response,
    "Failed to load solved problems"
  );
}

/* =========================================================
   TEST CASES
========================================================= */

export async function getProblemTestCases(problemId) {
  const response = await fetch(
    `${API_URL}/problems/${problemId}/test-cases`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  return readResponse(
    response,
    "Failed to load test cases"
  );
}

/* =========================================================
   CODE EXECUTION
========================================================= */

export async function runCode(
  problemId,
  code,
  input = ""
) {
  const response = await fetch(
    `${API_URL}/execution/run`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        problemId,
        code,
        input,
      }),
    }
  );

  return readResponse(
    response,
    "Failed to execute code"
  );
}

/* =========================================================
   SUBMISSIONS
========================================================= */

export async function submitProblem(
  problemId,
  code,
  language = "java"
) {
  const response = await fetch(
    `${API_URL}/submissions`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      credentials: "include",
      body: JSON.stringify({
        problemId,
        code,
        language,
      }),
    }
  );

  return readResponse(
    response,
    "Failed to submit solution"
  );
}

/* =========================================================
   SUBMISSION HISTORY
========================================================= */

export async function getMySubmissions() {
  const response = await fetch(
    `${API_URL}/submissions`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  return readResponse(
    response,
    "Failed to load submissions"
  );
}