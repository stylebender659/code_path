import { useEffect, useMemo, useState } from "react";
import "bootstrap/dist/css/bootstrap.min.css";

import {
  registerUser,
  loginUser,
  logoutUser,
  getCurrentUser,
  getTopics,
  getLessons,
  completeLesson,
  getProgress,
  getDashboard,
  getProblems,
  getProblem,
  getProblemTestCases,
  submitProblem,
  getMySubmissions,
  getSolvedProblemIds,
  runCode as executeCode,
} from "./api";

import "./index.css";

/* =========================================================
   ROADMAP
========================================================= */

const ROADMAP = [
  {
    step: "01",
    title: "Programming Basics",
    description:
      "Variables, loops, functions and problem-solving foundations.",
    target: "topics",
  },
  {
    step: "02",
    title: "Arrays & Strings",
    description:
      "Master the patterns behind the most common interview problems.",
    target: "topics",
  },
  {
    step: "03",
    title: "Linked Lists",
    description:
      "Understand nodes, traversal, pointers and list patterns.",
    target: "topics",
  },
  {
    step: "04",
    title: "Stacks & Queues",
    description:
      "Learn LIFO, FIFO and practical problem-solving patterns.",
    target: "topics",
  },
  {
    step: "05",
    title: "Trees & Graphs",
    description:
      "Build intuition for hierarchical and connected data.",
    target: "topics",
  },
  {
    step: "06",
    title: "Dynamic Programming",
    description:
      "Learn state transitions, memoization and tabulation.",
    target: "topics",
  },
];

/* =========================================================
   PIXEL WORLD BACKGROUND
========================================================= */

const BLOCKS = Array.from({ length: 42 }, (_, i) => ({
  left: `${(i * 47) % 101}%`,
  top: `${(i * 71) % 101}%`,
  size: i % 7 === 0 ? 7 : i % 3 === 0 ? 5 : 3,
  delay: `${(i % 9) * 0.4}s`,
}));

function PixelWorldBackdrop() {
  return (
    <div className="pixel-world" aria-hidden="true">
      <div className="world-haze world-haze-a" />
      <div className="world-haze world-haze-b" />
      <div className="world-grid" />

      <div className="world-blocks">
        {BLOCKS.map((block, index) => (
          <span
            key={index}
            className="world-block"
            style={{
              left: block.left,
              top: block.top,
              width: block.size,
              height: block.size,
              animationDelay: block.delay,
            }}
          />
        ))}
      </div>

      <div className="world-vignette" />
    </div>
  );
}

/* =========================================================
   MAIN APP
========================================================= */

function App() {
  /* -------------------------
     GENERAL
  ------------------------- */

  const [view, setView] = useState("home");
  const [mobileMenu, setMobileMenu] = useState(false);

  /* -------------------------
     AUTH
  ------------------------- */

  const [authMode, setAuthMode] = useState(null);
  const [user, setUser] = useState(null);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [authLoading, setAuthLoading] = useState(false);
  const [authError, setAuthError] = useState("");
  const [authMessage, setAuthMessage] = useState("");

  /* -------------------------
     TOPICS
  ------------------------- */

  const [topics, setTopics] = useState([]);
  const [topicsLoading, setTopicsLoading] = useState(true);
  const [topicsError, setTopicsError] = useState("");

  /* -------------------------
     LESSONS
  ------------------------- */

  const [selectedTopic, setSelectedTopic] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [selectedLessonIndex, setSelectedLessonIndex] = useState(0);

  const [lessonsLoading, setLessonsLoading] = useState(false);
  const [lessonsError, setLessonsError] = useState("");

  /* -------------------------
     PROGRESS
  ------------------------- */

  const [progress, setProgress] = useState(null);
  const [progressLoading, setProgressLoading] = useState(false);
  const [progressError, setProgressError] = useState("");
  const [completingLesson, setCompletingLesson] = useState(false);

  /* -------------------------
     DASHBOARD
  ------------------------- */

  const [dashboard, setDashboard] = useState(null);
  const [dashboardLoading, setDashboardLoading] = useState(false);
  const [dashboardError, setDashboardError] = useState("");

  /* -------------------------
     PROBLEMS
  ------------------------- */

  const [problems, setProblems] = useState([]);
  const [problemsLoading, setProblemsLoading] = useState(false);
  const [problemsError, setProblemsError] = useState("");
  const [problemSearch, setProblemSearch] = useState("");
  const [problemDifficulty, setProblemDifficulty] = useState("ALL");
  const [problemStatus, setProblemStatus] = useState("ALL");
  const [problemSort, setProblemSort] = useState("ORDER");
  const [solvedProblemIds, setSolvedProblemIds] = useState(new Set());

  const [selectedProblem, setSelectedProblem] = useState(null);
  const [problemLoading, setProblemLoading] = useState(false);
  const [problemError, setProblemError] = useState("");

  /* -------------------------
     CODE EDITOR
  ------------------------- */

  const [code, setCode] = useState("");
  const [copied, setCopied] = useState(false);

  /* -------------------------
     TEST CASES
  ------------------------- */

  const [testCases, setTestCases] = useState([]);
  const [testCasesLoading, setTestCasesLoading] = useState(false);
  const [testCasesError, setTestCasesError] = useState("");

  /* -------------------------
     SUBMISSIONS
  ------------------------- */

  const [submissions, setSubmissions] = useState([]);
  const [submissionsLoading, setSubmissionsLoading] = useState(false);
  const [submissionsError, setSubmissionsError] = useState("");

  const [submitLoading, setSubmitLoading] = useState(false);
  const [submitMessage, setSubmitMessage] = useState("");

  const [runMessage, setRunMessage] = useState("");
  const [executionStatus, setExecutionStatus] = useState("");
  const [executionOutput, setExecutionOutput] = useState("");

  /* =========================================================
     DERIVED DATA
  ========================================================= */

  const selectedLesson = lessons[selectedLessonIndex] || null;

  const filteredProblems = useMemo(() => {
    const query = problemSearch.trim().toLowerCase();

    const filtered = problems.filter((problem) => {
      const title = String(problem.title || "").toLowerCase();
      const description = String(problem.description || "").toLowerCase();
      const difficulty = String(problem.difficulty || "").toUpperCase();
      const solved = solvedProblemIds.has(problem.id);

      const matchesSearch =
        !query ||
        title.includes(query) ||
        description.includes(query);

      const matchesDifficulty =
        problemDifficulty === "ALL" ||
        difficulty === problemDifficulty;

      const matchesStatus =
        problemStatus === "ALL" ||
        (problemStatus === "SOLVED" && solved) ||
        (problemStatus === "UNSOLVED" && !solved);

      return matchesSearch && matchesDifficulty && matchesStatus;
    });

    return [...filtered].sort((a, b) => {
      if (problemSort === "TITLE") {
        return String(a.title || "").localeCompare(String(b.title || ""));
      }

      if (problemSort === "DIFFICULTY") {
        const rank = { EASY: 1, MEDIUM: 2, HARD: 3 };
        return (rank[String(a.difficulty || "").toUpperCase()] || 99) -
          (rank[String(b.difficulty || "").toUpperCase()] || 99);
      }

      return (Number(a.orderNumber) || 0) - (Number(b.orderNumber) || 0);
    });
  }, [
    problems,
    problemSearch,
    problemDifficulty,
    problemStatus,
    problemSort,
    solvedProblemIds,
  ]);

  const topicProgress = useMemo(() => {
    if (!progress?.lessons?.length || !lessons.length) {
      return 0;
    }

    const completedIds = new Set(
      progress.lessons
        .filter((item) => item.completed)
        .map((item) => item.lessonId)
    );

    const completedCount = lessons.filter((lesson) =>
      completedIds.has(lesson.id)
    ).length;

    return Math.round((completedCount / lessons.length) * 100);
  }, [progress, lessons]);

  /* =========================================================
     INITIAL LOAD
  ========================================================= */

  useEffect(() => {
    async function boot() {
      try {
        const currentUser = await getCurrentUser();

        if (currentUser) {
          setUser(currentUser);

          await Promise.all([
            loadProgress(),
            loadDashboard(),
            loadSolvedProblemIds(currentUser),
          ]);
        }
      } catch (error) {
        console.error("Session check failed:", error);
      }
    }

    boot();

    async function loadInitialTopics() {
      try {
        setTopicsLoading(true);
        setTopicsError("");

        const data = await getTopics();

        setTopics(Array.isArray(data) ? data : []);
      } catch (error) {
        setTopicsError(
          error.message || "Unable to load topics."
        );
      } finally {
        setTopicsLoading(false);
      }
    }

    loadInitialTopics();
  }, []);

  /* =========================================================
     HOME HASH
  ========================================================= */

  useEffect(() => {
    const hash = window.location.hash.replace("#", "");

    if (
      hash &&
      ["home", "topics", "roadmap", "how-it-works"].includes(hash)
    ) {
      requestAnimationFrame(() => {
        document
          .getElementById(hash)
          ?.scrollIntoView({
            behavior: "smooth",
          });
      });
    }
  }, [view]);

  /* =========================================================
     PROGRESS
  ========================================================= */

  async function loadProgress() {
    try {
      setProgressLoading(true);
      setProgressError("");

      const data = await getProgress();

      setProgress(data);
    } catch (error) {
      setProgressError(
        error.message || "Unable to load progress."
      );
    } finally {
      setProgressLoading(false);
    }
  }

  /* =========================================================
     DASHBOARD
  ========================================================= */

  async function loadDashboard() {
    try {
      setDashboardLoading(true);
      setDashboardError("");

      const data = await getDashboard();

      setDashboard(data);
    } catch (error) {
      setDashboardError(
        error.message || "Unable to load dashboard."
      );
    } finally {
      setDashboardLoading(false);
    }
  }

  /* =========================================================
     AUTH
  ========================================================= */

  function openAuth(mode) {
    setAuthMode(mode);
    setAuthError("");
    setAuthMessage("");
    setPassword("");
  }

  function closeAuth() {
    if (authLoading) {
      return;
    }

    setAuthMode(null);
    setAuthError("");
    setAuthMessage("");
  }

  async function handleRegister(event) {
    event.preventDefault();

    setAuthLoading(true);
    setAuthError("");
    setAuthMessage("");

    try {
      const data = await registerUser(
        name.trim(),
        email.trim(),
        password
      );

      setUser(data);

      await Promise.all([
        loadProgress(),
        loadDashboard(),
        loadSolvedProblemIds(data),
        loadSubmissions(data),
      ]);

      setAuthMessage("Account created successfully.");

      setTimeout(() => {
        closeAuth();
      }, 700);
    } catch (error) {
      setAuthError(
        error.message || "Registration failed."
      );
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleLogin(event) {
    event.preventDefault();

    setAuthLoading(true);
    setAuthError("");
    setAuthMessage("");

    try {
      const data = await loginUser(
        email.trim(),
        password
      );

      setUser(data);

      await Promise.all([
        loadProgress(),
        loadDashboard(),
        loadSolvedProblemIds(data),
        loadSubmissions(data),
      ]);

      setAuthMessage("Welcome back.");

      setTimeout(() => {
        closeAuth();
      }, 700);
    } catch (error) {
      setAuthError(
        error.message || "Login failed."
      );
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleLogout() {
    try {
      await logoutUser();
    } catch (error) {
      console.error("Logout failed:", error);
    } finally {
      setUser(null);

      setProgress(null);
      setDashboard(null);

      setSelectedTopic(null);
      setLessons([]);

      setSelectedProblem(null);
      setTestCases([]);
      setSubmissions([]);
      setSolvedProblemIds(new Set());

      setView("home");
      setMobileMenu(false);

      window.history.replaceState(
        null,
        "",
        "#home"
      );

      window.scrollTo({
        top: 0,
        behavior: "smooth",
      });
    }
  }

  /* =========================================================
     NAVIGATION
  ========================================================= */

  function goHome() {
    setView("home");

    setSelectedTopic(null);
    setSelectedProblem(null);

    setMobileMenu(false);

    window.history.replaceState(
      null,
      "",
      "#home"
    );

    requestAnimationFrame(() => {
      window.scrollTo({
        top: 0,
        behavior: "smooth",
      });
    });
  }

  function scrollHomeSection(id) {
    setView("home");

    setSelectedTopic(null);
    setSelectedProblem(null);

    setMobileMenu(false);

    window.history.replaceState(
      null,
      "",
      `#${id}`
    );

    requestAnimationFrame(() => {
      document
        .getElementById(id)
        ?.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
    });
  }

  async function openDashboard() {
    if (!user) {
      openAuth("login");
      return;
    }

    setView("dashboard");
    setMobileMenu(false);

    await Promise.all([
      loadDashboard(),
      loadProgress(),
      loadSolvedProblemIds(),
      loadSubmissions(),
    ]);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  function openTopics() {
    setView("home");

    setSelectedTopic(null);
    setSelectedProblem(null);

    setMobileMenu(false);

    window.history.replaceState(
      null,
      "",
      "#topics"
    );

    requestAnimationFrame(() => {
      document
        .getElementById("topics")
        ?.scrollIntoView({
          behavior: "smooth",
        });
    });
  }

  /* =========================================================
     TOPICS
  ========================================================= */

  async function openTopic(topic) {
    setSelectedTopic(topic);
    setSelectedProblem(null);

    setLessons([]);
    setSelectedLessonIndex(0);

    setLessonsError("");
    setLessonsLoading(true);

    setView("lesson");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });

    try {
      const data = await getLessons(topic.id);

      setLessons(
        Array.isArray(data) ? data : []
      );
    } catch (error) {
      setLessonsError(
        error.message || "Unable to load lessons."
      );
    } finally {
      setLessonsLoading(false);
    }
  }

  function closeLesson() {
    setSelectedTopic(null);
    setLessons([]);
    setSelectedLessonIndex(0);

    setView("home");

    openTopics();
  }

  function selectLesson(index) {
    if (
      index < 0 ||
      index >= lessons.length
    ) {
      return;
    }

    setSelectedLessonIndex(index);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  function previousLesson() {
    if (selectedLessonIndex > 0) {
      selectLesson(
        selectedLessonIndex - 1
      );
    }
  }

  function nextLesson() {
    if (
      selectedLessonIndex <
      lessons.length - 1
    ) {
      selectLesson(
        selectedLessonIndex + 1
      );
    }
  }

  /* =========================================================
     COMPLETE LESSON
  ========================================================= */

  async function markComplete() {
    if (!user) {
      openAuth("login");
      return;
    }

    if (
      !selectedLesson ||
      completingLesson
    ) {
      return;
    }

    try {
      setCompletingLesson(true);
      setProgressError("");

      await completeLesson(
        selectedLesson.id
      );

      await Promise.all([
        loadProgress(),
        loadDashboard(),
        loadSolvedProblemIds(),
      ]);
    } catch (error) {
      setProgressError(
        error.message ||
          "Unable to save progress."
      );
    } finally {
      setCompletingLesson(false);
    }
  }

  function isCompleted(id) {
    return Boolean(
      progress?.lessons?.some(
        (item) =>
          item.lessonId === id &&
          item.completed
      )
    );
  }

  /* =========================================================
     PROBLEMS
  ========================================================= */

  async function openProblems() {
    setView("problems");

    setSelectedProblem(null);
    setProblemSearch("");
    setProblemDifficulty("ALL");
    setProblemStatus("ALL");
    setProblemSort("ORDER");

    setProblemsLoading(true);
    setProblemsError("");

    setMobileMenu(false);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });

    try {
      const data = await getProblems();

      setProblems(
        Array.isArray(data) ? data : []
      );

      await loadSolvedProblemIds();
      await loadSubmissions();
    } catch (error) {
      setProblemsError(
        error.message ||
          "Unable to load problems."
      );
    } finally {
      setProblemsLoading(false);
    }
  }

  /* =========================================================
     OPEN PROBLEM
  ========================================================= */

  async function openProblem(problem) {
    setSelectedProblem(problem);

    setProblemError("");
    setRunMessage("");
    setSubmitMessage("");

    setCopied(false);

    setTestCases([]);
    setTestCasesError("");

    setSubmissions([]);
    setSubmissionsError("");

    setView("problem");

    setProblemLoading(true);

    setTestCasesLoading(true);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });

    try {
      const full = await getProblem(
        problem.id
      );

      setSelectedProblem(full);

      setCode(
        full.starterCode || ""
      );
    } catch (error) {
      setProblemError(
        error.message ||
          "Unable to load this problem."
      );

      setCode(
        problem.starterCode || ""
      );
    } finally {
      setProblemLoading(false);
    }

    try {
      const data =
        await getProblemTestCases(
          problem.id
        );

      setTestCases(
        Array.isArray(data)
          ? data
          : []
      );
    } catch (error) {
      setTestCasesError(
        error.message ||
          "Unable to load test cases."
      );
    } finally {
      setTestCasesLoading(false);
    }

    if (user) {
      await loadSubmissions();
    }
  }

  function closeProblem() {
    setSelectedProblem(null);

    setProblemError("");
    setRunMessage("");
    setSubmitMessage("");

    setTestCases([]);
    setSubmissions([]);

    setView("problems");

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  }

  /* =========================================================
     CODE EDITOR
  ========================================================= */

  async function copyCode() {
    try {
      await navigator.clipboard.writeText(
        code
      );

      setCopied(true);

      setTimeout(() => {
        setCopied(false);
      }, 1400);
    } catch {
      setRunMessage(
        "Copy is unavailable in this browser context."
      );
    }
  }

  function resetCode() {
    setCode(
      selectedProblem?.starterCode || ""
    );

    setRunMessage(
      "Starter code restored."
    );

    setSubmitMessage("");
  }

  async function runCode() {
    if (!code.trim()) {
      setRunMessage("Add code before running.");
      setExecutionStatus("");
      setExecutionOutput("");
      return;
    }

    if (!selectedProblem) {
      setRunMessage("No problem selected.");
      return;
    }

    try {
      setRunMessage("Running your code...");
      setExecutionStatus("RUNNING");
      setExecutionOutput("");

      const firstTestCase = testCases?.[0];

      const result = await executeCode(
        selectedProblem.id,
        code,
        firstTestCase?.input || ""
      );

      setExecutionStatus(result.status || "UNKNOWN");
      setExecutionOutput(
        result.output || "Program finished without output."
      );

      setRunMessage(
        result.status === "SUCCESS"
          ? "Execution completed successfully."
          : "Execution completed. Check the output below."
      );
    } catch (error) {
      setExecutionStatus("SYSTEM_ERROR");
      setExecutionOutput(
        error.message || "Code execution failed."
      );
      setRunMessage("Execution failed. Check the output below.");
    }
  }

  /* =========================================================
     SUBMISSIONS
  ========================================================= */

  async function loadSolvedProblemIds(userOverride = user) {
    if (!userOverride) {
      setSolvedProblemIds(new Set());
      return;
    }

    try {
      const data = await getSolvedProblemIds();

      const ids = Array.isArray(data)
        ? data.map(Number).filter(Number.isFinite)
        : [];

      setSolvedProblemIds(new Set(ids));
    } catch (error) {
      console.error(
        "Failed to load solved problem IDs:",
        error
      );
    }
  }

  async function loadSubmissions(userOverride = user) {
    if (!userOverride) {
      setSubmissions([]);
      return;
    }

    try {
      setSubmissionsLoading(true);
      setSubmissionsError("");

      const data =
        await getMySubmissions();

      setSubmissions(
        Array.isArray(data)
          ? data
          : []
      );
    } catch (error) {
      setSubmissionsError(
        error.message ||
          "Unable to load submissions."
      );
    } finally {
      setSubmissionsLoading(false);
    }
  }

  async function handleSubmit() {
    if (!user) {
      openAuth("login");
      return;
    }

    if (!selectedProblem) {
      return;
    }

    if (!code.trim()) {
      setSubmitMessage(
        "Write some code before submitting."
      );
      return;
    }

    try {
      setSubmitLoading(true);
      setSubmitMessage("");

      const result =
        await submitProblem(
          selectedProblem.id,
          code,
          "java"
        );

      setSubmitMessage(
        `Submission #${result.id ?? ""} created successfully. Status: ${
          result.status || "QUEUED"
        }`
      );

      if (result.status === "ACCEPTED" && selectedProblem?.id != null) {
        setSolvedProblemIds((current) => {
          const next = new Set(current);
          next.add(Number(selectedProblem.id));
          return next;
        });
      }

      await Promise.all([
        loadSubmissions(),
        loadSolvedProblemIds(),
        loadDashboard(),
      ]);
    } catch (error) {
      setSubmitMessage(
        error.message ||
          "Failed to submit solution."
      );
    } finally {
      setSubmitLoading(false);
    }
  }

  /* =========================================================
     RENDER
  ========================================================= */

  return (
    <div className="app-shell">
      <PixelWorldBackdrop />

      {/* =====================================================
          HEADER
      ===================================================== */}

      <header className="site-header">
        <div className="site-container header-inner">

          <button
            className="brand"
            onClick={goHome}
            aria-label="Go to CodePath home"
          >
            <span className="brand-mark">
              &lt;/&gt;
            </span>

            <span>CodePath</span>
          </button>

          <button
            className="mobile-toggle"
            onClick={() =>
              setMobileMenu(
                (open) => !open
              )
            }
            aria-label="Toggle navigation"
            aria-expanded={mobileMenu}
          >
            <span />
            <span />
            <span />
          </button>

          <nav
            className={`main-nav ${
              mobileMenu
                ? "is-open"
                : ""
            }`}
          >
            <button
              onClick={() =>
                scrollHomeSection(
                  "home"
                )
              }
            >
              Home
            </button>

            <button
              onClick={openTopics}
            >
              Topics
            </button>

            <button
              onClick={() =>
                scrollHomeSection(
                  "roadmap"
                )
              }
            >
              Roadmap
            </button>

            <button
              onClick={() =>
                scrollHomeSection(
                  "how-it-works"
                )
              }
            >
              How It Works
            </button>

            <button
              onClick={openProblems}
            >
              Problems
            </button>
          </nav>

          <div className="header-actions">
            {user ? (
              <>
                <button
                  className="header-dashboard"
                  onClick={openDashboard}
                >
                  Dashboard
                </button>

                <button
                  className="button button-small button-outline"
                  onClick={
                    handleLogout
                  }
                >
                  Log Out
                </button>
              </>
            ) : (
              <>
                <button
                  className="header-login"
                  onClick={() =>
                    openAuth("login")
                  }
                >
                  Log In
                </button>

                <button
                  className="button button-small"
                  onClick={() =>
                    openAuth(
                      "register"
                    )
                  }
                >
                  Get Started
                </button>
              </>
            )}
          </div>
        </div>
      </header>

      {/* =====================================================
          HOME
      ===================================================== */}

      {view === "home" && (
        <main>

          {/* HERO */}

          <section
            id="home"
            className="hero section"
          >
            <div className="site-container hero-grid">

              <div className="hero-copy">

                <span className="eyebrow">
                  DSA LEARNING SYSTEM // 01
                </span>

                <h1>
                  Build the logic.
                  <br />
                  <span>
                    Master the path.
                  </span>
                </h1>

                <p>
                  A structured place
                  to learn Data
                  Structures and
                  Algorithms, practice
                  problems, and track
                  your progress without
                  the clutter.
                </p>

                <div className="hero-actions">

                  <button
                    className="button button-large"
                    onClick={() =>
                      user
                        ? openTopics()
                        : openAuth(
                            "register"
                          )
                    }
                  >
                    {user
                      ? "Continue Learning"
                      : "Start Learning"}

                    <span>→</span>
                  </button>

                  <button
                    className="button button-large button-quiet"
                    onClick={
                      openProblems
                    }
                  >
                    Practice Problems
                  </button>

                </div>

                <div className="hero-meta">
                  <span>
                    JAVA FOCUSED
                  </span>

                  <span>
                    STRUCTURED LESSONS
                  </span>

                  <span>
                    PROGRESS TRACKING
                  </span>
                </div>

              </div>

              <div className="hero-console">

                <div className="console-top">
                  <span className="console-lights">
                    <i />
                    <i />
                    <i />
                  </span>

                  <span>
                    CODEPATH / TERMINAL
                  </span>

                  <span>
                    READY
                  </span>
                </div>

                <pre>{`public class Main {
  public static void main(String[] args) {
    int[] data = {4, 8, 15, 16, 23, 42};

    for (int value : data) {
      System.out.println(value);
    }
  }
}`}</pre>

                <div className="console-bottom">
                  <span>JAVA</span>
                  <span>BUILD 01</span>
                  <span>
                    ● ONLINE
                  </span>
                </div>

              </div>

            </div>
          </section>

          {/* STATS */}

          <section className="stats-strip">
            <div className="site-container stats-grid">

              <div>
                <strong>
                  {topics.length
                    ? String(
                        topics.length
                      ).padStart(
                        2,
                        "0"
                      )
                    : "08"}
                </strong>

                <span>
                  Learning Topics
                </span>
              </div>

              <div>
                <strong>
                  40
                </strong>

                <span>
                  Lessons
                </span>
              </div>

              <div>
                <strong>
                  {problems.length ||
                    10}
                </strong>

                <span>
                  Coding Problems
                </span>
              </div>

              <div>
                <strong>
                  01
                </strong>

                <span>
                  Focused Path
                </span>
              </div>

            </div>
          </section>

          {/* TOPICS */}

          <section
            id="topics"
            className="section section-light"
          >
            <div className="site-container">

              <div className="section-heading">
                <div>
                  <span className="eyebrow">
                    LEARN BY TOPIC // 02
                  </span>

                  <h2>
                    Choose your next
                    block.
                  </h2>
                </div>

                <p>
                  Each topic is backed
                  by lessons stored in
                  your CodePath
                  database.
                </p>
              </div>

              {topicsLoading ? (
                <LoadingState
                  text="Loading topics..."
                />
              ) : topicsError ? (
                <ErrorState
                  text={topicsError}
                  onRetry={() =>
                    window.location.reload()
                  }
                />
              ) : (
                <div className="topic-grid">

                  {topics.map(
                    (topic, index) => (
                      <button
                        className="topic-card"
                        key={topic.id}
                        onClick={() =>
                          openTopic(
                            topic
                          )
                        }
                      >
                        <span className="topic-number">
                          {String(
                            index + 1
                          ).padStart(
                            2,
                            "0"
                          )}
                        </span>

                        <span className="topic-content">
                          <strong>
                            {topic.title}
                          </strong>

                          <span>
                            {
                              topic.description
                            }
                          </span>
                        </span>

                        <span className="topic-footer">
                          <em>
                            {
                              topic.difficulty
                            }
                          </em>

                          <b>
                            OPEN →
                          </b>
                        </span>
                      </button>
                    )
                  )}

                </div>
              )}

            </div>
          </section>

          {/* ROADMAP */}

          <section
            id="roadmap"
            className="section"
          >
            <div className="site-container">

              <div className="section-heading">

                <div>
                  <span className="eyebrow">
                    ROADMAP // 03
                  </span>

                  <h2>
                    Follow a clear
                    progression.
                  </h2>
                </div>

                <p>
                  Start with fundamentals
                  and build toward
                  interview-level problem
                  solving.
                </p>

              </div>

              <div className="roadmap-list">

                {ROADMAP.map(
                  (item, index) => (
                    <button
                      className={`roadmap-item ${
                        index === 0
                          ? "active"
                          : ""
                      }`}
                      key={item.step}
                      onClick={() =>
                        scrollHomeSection(
                          item.target
                        )
                      }
                    >
                      <span className="roadmap-step">
                        {item.step}
                      </span>

                      <span className="roadmap-copy">
                        <strong>
                          {item.title}
                        </strong>

                        <span>
                          {
                            item.description
                          }
                        </span>
                      </span>

                      <span className="roadmap-status">
                        {index === 0
                          ? "OPEN"
                          : "VIEW TOPICS"}{" "}
                        →
                      </span>
                    </button>
                  )
                )}

              </div>
            </div>
          </section>

          {/* HOW IT WORKS */}

          <section
            id="how-it-works"
            className="section section-light"
          >
            <div className="site-container">

              <div className="section-heading">
                <div>
                  <span className="eyebrow">
                    HOW IT WORKS // 04
                  </span>

                  <h2>
                    Learn. Practice.
                    Track.
                  </h2>
                </div>
              </div>

              <div className="flow-grid">

                <button
                  className="flow-card"
                  onClick={openTopics}
                >
                  <span>01</span>

                  <strong>
                    Learn
                  </strong>

                  <p>
                    Open a topic and
                    work through focused
                    lessons.
                  </p>

                  <b>
                    OPEN TOPICS →
                  </b>
                </button>

                <button
                  className="flow-card"
                  onClick={
                    openProblems
                  }
                >
                  <span>02</span>

                  <strong>
                    Practice
                  </strong>

                  <p>
                    Move from concepts
                    into structured
                    coding problems.
                  </p>

                  <b>
                    OPEN PROBLEMS →
                  </b>
                </button>

                <button
                  className="flow-card"
                  onClick={
                    openDashboard
                  }
                >
                  <span>03</span>

                  <strong>
                    Track
                  </strong>

                  <p>
                    See your saved
                    lesson progress and
                    dashboard statistics.
                  </p>

                  <b>
                    OPEN DASHBOARD →
                  </b>
                </button>

              </div>
            </div>
          </section>

          {/* CTA */}

          <section className="cta">
            <div className="site-container cta-inner">

              <div>
                <span className="eyebrow">
                  READY // 05
                </span>

                <h2>
                  Make the next
                  problem count.
                </h2>

                <p>
                  Start learning or jump
                  straight into the
                  practice system.
                </p>
              </div>

              <button
                className="button button-large"
                onClick={() =>
                  user
                    ? openProblems()
                    : openAuth(
                        "register"
                      )
                }
              >
                {user
                  ? "Practice Now"
                  : "Create Free Account"}

                <span>→</span>
              </button>

            </div>
          </section>

        </main>
      )}

      {/* =====================================================
          DASHBOARD
      ===================================================== */}

      {view === "dashboard" && (
        <DashboardView
          user={user}
          dashboard={dashboard}
          loading={dashboardLoading}
          error={dashboardError}
          progress={progress}
          progressLoading={
            progressLoading
          }
          submissions={submissions}
          submissionsLoading={submissionsLoading}
          submissionsError={submissionsError}
          onBack={goHome}
          onTopics={openTopics}
          onProblems={
            openProblems
          }
          onRefresh={() =>
            Promise.all([
              loadDashboard(),
              loadProgress(),
              loadSolvedProblemIds(),
              loadSubmissions(),
            ])
          }
        />
      )}

      {/* =====================================================
          LESSON
      ===================================================== */}

      {view === "lesson" && (
        <LessonView
          user={user}
          topic={selectedTopic}
          lessons={lessons}
          lesson={selectedLesson}
          index={selectedLessonIndex}
          loading={lessonsLoading}
          error={lessonsError}
          topicProgress={
            topicProgress
          }
          progress={progress}
          progressError={
            progressError
          }
          onBack={closeLesson}
          onSelect={selectLesson}
          onPrevious={
            previousLesson
          }
          onNext={nextLesson}
          onComplete={
            markComplete
          }
          completed={
            selectedLesson
              ? isCompleted(
                  selectedLesson.id
                )
              : false
          }
          completing={
            completingLesson
          }
          onLogin={() =>
            openAuth("login")
          }
        />
      )}

      {/* =====================================================
          PROBLEMS
      ===================================================== */}

      {view === "problems" && (
        <ProblemsView
          problems={problems}
          filteredProblems={filteredProblems}
          loading={problemsLoading}
          error={problemsError}
          search={problemSearch}
          difficulty={problemDifficulty}
          status={problemStatus}
          sort={problemSort}
          solvedProblemIds={solvedProblemIds}
          onSearch={setProblemSearch}
          onDifficulty={setProblemDifficulty}
          onStatus={setProblemStatus}
          onSort={setProblemSort}
          onBack={goHome}
          onRetry={openProblems}
          onSolve={openProblem}
        />
      )}

      {/* =====================================================
          PROBLEM
      ===================================================== */}

      {view === "problem" && (
        <ProblemView
          user={user}
          problem={selectedProblem}
          loading={problemLoading}
          error={problemError}
          code={code}
          setCode={setCode}
          copied={copied}
          runMessage={runMessage}
          executionStatus={executionStatus}
          executionOutput={executionOutput}
          testCases={testCases}
          testCasesLoading={
            testCasesLoading
          }
          testCasesError={
            testCasesError
          }
          submissions={submissions}
          submissionsLoading={
            submissionsLoading
          }
          submissionsError={
            submissionsError
          }
          submitLoading={
            submitLoading
          }
          submitMessage={
            submitMessage
          }
          onBack={closeProblem}
          onCopy={copyCode}
          onReset={resetCode}
          onRun={runCode}
          onSubmit={
            handleSubmit
          }
          onLogin={() =>
            openAuth("login")
          }
        />
      )}

      {/* =====================================================
          FOOTER
      ===================================================== */}

      {view === "home" && (
        <footer className="site-footer">

          <div className="site-container footer-grid">

            <div>
              <button
                className="brand footer-brand"
                onClick={goHome}
              >
                <span className="brand-mark">
                  &lt;/&gt;
                </span>

                <span>
                  CodePath
                </span>
              </button>

              <p>
                Structured DSA learning,
                practice and progress
                tracking.
              </p>
            </div>

            <div>
              <strong>
                LEARN
              </strong>

              <button
                onClick={openTopics}
              >
                Topics
              </button>

              <button
                onClick={() =>
                  scrollHomeSection(
                    "roadmap"
                  )
                }
              >
                Roadmap
              </button>

              <button
                onClick={() =>
                  scrollHomeSection(
                    "how-it-works"
                  )
                }
              >
                How It Works
              </button>
            </div>

            <div>
              <strong>
                PRACTICE
              </strong>

              <button
                onClick={
                  openProblems
                }
              >
                Problems
              </button>

              <button
                onClick={
                  openDashboard
                }
              >
                Dashboard
              </button>
            </div>

            <div>
              <strong>
                ACCOUNT
              </strong>

              {user ? (
                <button
                  onClick={
                    handleLogout
                  }
                >
                  Log Out
                </button>
              ) : (
                <>
                  <button
                    onClick={() =>
                      openAuth("login")
                    }
                  >
                    Log In
                  </button>

                  <button
                    onClick={() =>
                      openAuth(
                        "register"
                      )
                    }
                  >
                    Create Account
                  </button>
                </>
              )}
            </div>

          </div>

          <div className="site-container footer-bottom">
            <span>
              © 2026 CodePath
            </span>

            <span>
              Learn DSA. Build
              skills.
            </span>
          </div>

        </footer>
      )}

      {/* =====================================================
          AUTH MODAL
      ===================================================== */}

      {authMode && (
        <AuthModal
          mode={authMode}
          name={name}
          setName={setName}
          email={email}
          setEmail={setEmail}
          password={password}
          setPassword={
            setPassword
          }
          loading={authLoading}
          error={authError}
          message={authMessage}
          onClose={closeAuth}
          onSwitch={openAuth}
          onLogin={handleLogin}
          onRegister={
            handleRegister
          }
        />
      )}
    </div>
  );
}

/* =========================================================
   DASHBOARD VIEW
========================================================= */

function DashboardView({
  user,
  dashboard,
  loading,
  error,
  progress,
  progressLoading,
  submissions,
  submissionsLoading,
  submissionsError,
  onBack,
  onTopics,
  onProblems,
  onRefresh,
}) {
  return (
    <PageFrame
      title="Your Dashboard"
      kicker="PLAYER PROFILE // 06"
      onBack={onBack}
    >
      {loading || progressLoading ? (
        <LoadingState
          text="Syncing your progress..."
        />
      ) : error ? (
        <ErrorState
          text={error}
          onRetry={onRefresh}
        />
      ) : (
        <>
          <div className="dashboard-welcome">

            <div>
              <span className="eyebrow">
                WELCOME BACK
              </span>

              <h1>
                {user?.name ||
                  "Learner"}
              </h1>

              <p>
                Your learning state
                is saved in the
                CodePath backend.
              </p>
            </div>

            <div className="dashboard-actions">

              <button
                className="button"
                onClick={onTopics}
              >
                Continue Learning →
              </button>

              <button
                className="button button-quiet"
                onClick={
                  onProblems
                }
              >
                Practice Problems
              </button>

            </div>
          </div>

          <div className="dashboard-stats">

            <Stat
              label="Overall Progress"
              value={`${Math.round(
                dashboard?.overallProgress ||
                  progress?.percentage ||
                  0
              )}%`}
            />

            <Stat
              label="Lessons Completed"
              value={
                dashboard?.completedLessons ??
                progress?.completedLessons ??
                0
              }
            />

            <Stat
              label="Problems Solved"
              value={
                dashboard?.problemsSolved ??
                0
              }
            />

            <Stat
              label="Current Streak"
              value={
                dashboard?.currentStreak ??
                0
              }
            />

          </div>

          <section className="dashboard-panel">

            <div className="panel-heading">
              <span>
                TOPIC PROGRESS
              </span>

              <button
                onClick={onRefresh}
              >
                Refresh ↻
              </button>
            </div>

            <div className="dashboard-topic-list">

              {(dashboard?.topics ||
                []).map(
                (topic, index) => (
                  <div
                    className="dashboard-topic"
                    key={`${topic.name}-${index}`}
                  >
                    <div>
                      <strong>
                        {topic.name}
                      </strong>

                      <span>
                        {Math.round(
                          topic.progress ||
                            0
                        )}
                        %
                      </span>
                    </div>

                    <div className="progress-track">
                      <i
                        style={{
                          width: `${Math.min(
                            100,
                            Math.max(
                              0,
                              topic.progress ||
                                0
                            )
                          )}%`,
                        }}
                      />
                    </div>
                  </div>
                )
              )}

              {!dashboard?.topics
                ?.length && (
                <p className="muted">
                  Topic progress will
                  appear after your
                  first saved lesson.
                </p>
              )}

            </div>
          </section>

          <section className="dashboard-panel submission-panel">
            <div className="panel-heading">
              <span>RECENT SUBMISSIONS</span>
              <button onClick={onRefresh}>Refresh ↻</button>
            </div>

            {submissionsLoading ? (
              <LoadingState text="Loading recent submissions..." />
            ) : submissionsError ? (
              <ErrorState text={submissionsError} onRetry={onRefresh} />
            ) : !submissions?.length ? (
              <div className="empty-panel">
                <h2>No submissions yet.</h2>
                <p>Solve a problem and your latest attempts will appear here.</p>
              </div>
            ) : (
              <div className="submission-list">
                {submissions.slice(0, 5).map((submission) => (
                  <div className="submission-row" key={submission.id}>
                    <div>
                      <strong>#{submission.id}</strong>
                      <span>{submission.problemTitle || "Problem"}</span>
                    </div>
                    <div>
                      <span>{submission.language || "java"}</span>
                      <span
                        className={`submission-status ${String(
                          submission.status || "QUEUED"
                        ).toLowerCase()}`}
                      >
                        {submission.status || "QUEUED"}
                      </span>
                      <small>
                        {submission.createdAt
                          ? new Date(submission.createdAt).toLocaleString()
                          : ""}
                      </small>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </PageFrame>
  );
}

/* =========================================================
   LESSON VIEW
========================================================= */

function LessonView({
  user,
  topic,
  lessons,
  lesson,
  index,
  loading,
  error,
  topicProgress,
  progress,
  progressError,
  onBack,
  onSelect,
  onPrevious,
  onNext,
  onComplete,
  completed,
  completing,
  onLogin,
}) {
  return (
    <PageFrame
      title={topic?.title || "Lessons"}
      kicker="LEARN BY TOPIC // 07"
      onBack={onBack}
    >
      <div className="lesson-heading">

        <div>
          <span className="eyebrow">
            {topic?.difficulty ||
              "DSA"}{" "}
            · {lessons.length}{" "}
            LESSONS
          </span>

          <h1>
            {topic?.title}
          </h1>

          <p>
            {topic?.description}
          </p>
        </div>

        <div className="topic-progress-box">
          <strong>
            {topicProgress}%
          </strong>

          <span>
            TOPIC PROGRESS
          </span>
        </div>

      </div>

      {loading ? (
        <LoadingState
          text="Loading lessons..."
        />
      ) : error ? (
        <ErrorState
          text={error}
          onRetry={() =>
            window.location.reload()
          }
        />
      ) : !lessons.length ? (
        <div className="empty-panel">
          <h2>
            No lessons yet.
          </h2>

          <p>
            This topic has not
            been seeded with
            lessons.
          </p>
        </div>
      ) : (
        <div className="lesson-layout">

          <aside className="lesson-sidebar">

            <div className="panel-heading">
              <span>
                COURSE CONTENT
              </span>

              <b>
                {lessons.length}
              </b>
            </div>

            <div className="lesson-list">

              {lessons.map(
                (
                  item,
                  lessonIndex
                ) => (
                  <button
                    key={item.id}
                    className={`lesson-item ${
                      lessonIndex ===
                      index
                        ? "active"
                        : ""
                    }`}
                    onClick={() =>
                      onSelect(
                        lessonIndex
                      )
                    }
                  >
                    <span>
                      {String(
                        lessonIndex + 1
                      ).padStart(
                        2,
                        "0"
                      )}
                    </span>

                    <strong>
                      {item.title}
                    </strong>

                    <b>
                      {user &&
                      progress?.lessons?.some(
                        (entry) =>
                          entry.lessonId ===
                            item.id &&
                          entry.completed
                      )
                        ? "✓"
                        : "•"}
                    </b>
                  </button>
                )
              )}

            </div>
          </aside>

          <article className="lesson-content">

            <div className="lesson-content-top">

              <span className="eyebrow">
                LESSON{" "}
                {String(
                  index + 1
                ).padStart(
                  2,
                  "0"
                )}
              </span>

              <h2>
                {lesson?.title}
              </h2>

            </div>

            <div className="lesson-body">
              <LessonText
                content={
                  lesson?.content ||
                  ""
                }
              />
            </div>

            {progressError && (
              <div className="inline-error">
                {progressError}
              </div>
            )}

            <div className="lesson-complete-row">

              {user ? (
                <button
                  className={`button ${
                    completed
                      ? "button-success"
                      : ""
                  }`}
                  onClick={
                    onComplete
                  }
                  disabled={
                    completed ||
                    completing
                  }
                >
                  {completed
                    ? "✓ Completed"
                    : completing
                    ? "Saving..."
                    : "Mark Lesson Complete"}
                </button>
              ) : (
                <button
                  className="button"
                  onClick={onLogin}
                >
                  Log in to save
                  progress
                </button>
              )}

            </div>

            <div className="lesson-nav">

              <button
                className="lesson-nav-button"
                onClick={
                  onPrevious
                }
                disabled={
                  index === 0
                }
              >
                <span>←</span>

                <div>
                  <small>
                    PREVIOUS
                  </small>

                  <strong>
                    {index > 0
                      ? lessons[
                          index - 1
                        ].title
                      : "First lesson"}
                  </strong>
                </div>
              </button>

              <button
                className="lesson-nav-button"
                onClick={onNext}
                disabled={
                  index >=
                  lessons.length - 1
                }
              >
                <div>
                  <small>
                    NEXT
                  </small>

                  <strong>
                    {index <
                    lessons.length -
                      1
                      ? lessons[
                          index + 1
                        ].title
                      : "Last lesson"}
                  </strong>
                </div>

                <span>→</span>
              </button>

            </div>

          </article>

        </div>
      )}
    </PageFrame>
  );
}

/* =========================================================
   LESSON TEXT
========================================================= */

function LessonText({ content }) {
  const lines = content.split(
    /\r?\n/
  );

  return (
    <div className="lesson-text">
      {lines.map(
        (line, index) => {
          const trimmed =
            line.trim();

          if (!trimmed) {
            return (
              <div
                className="lesson-space"
                key={index}
              />
            );
          }

          if (
            /^(Example|Examples|Constraints|Approach|Theory|Note|Important)\s*:?\s*$/i.test(
              trimmed
            )
          ) {
            return (
              <h3 key={index}>
                {trimmed.replace(
                  /:$/,
                  ""
                )}
              </h3>
            );
          }

          if (
            /^```/.test(trimmed)
          ) {
            return null;
          }

          return (
            <p key={index}>
              {line}
            </p>
          );
        }
      )}
    </div>
  );
}

/* =========================================================
   PROBLEMS VIEW
========================================================= */

function ProblemsView({
  problems,
  filteredProblems,
  loading,
  error,
  search,
  difficulty,
  status,
  sort,
  solvedProblemIds,
  onSearch,
  onDifficulty,
  onStatus,
  onSort,
  onBack,
  onRetry,
  onSolve,
}) {
  return (
    <PageFrame
      title="Practice Problems"
      kicker="CODE // PRACTICE // 08"
      onBack={onBack}
    >
      <div className="problems-intro">
        <div>
          <h1>Sharpen the logic.</h1>
          <p>
            Search the problem set, filter by difficulty or completion,
            and jump directly into the coding workspace.
          </p>
        </div>

        <div className="problem-counter">
          <strong>{filteredProblems.length}</strong>
          <span>
            {filteredProblems.length === problems.length
              ? "AVAILABLE"
              : `OF ${problems.length}`}
          </span>
        </div>
      </div>

      {!loading && !error && problems.length > 0 && (
        <section className="problem-controls" aria-label="Problem filters">
          <div className="problem-search-wrap">
            <label htmlFor="problem-search">SEARCH</label>
            <input
              id="problem-search"
              className="problem-search"
              type="search"
              value={search}
              onChange={(event) => onSearch(event.target.value)}
              placeholder="Search by title or description..."
            />
          </div>

          <div className="problem-filter">
            <label htmlFor="problem-difficulty">DIFFICULTY</label>
            <select
              id="problem-difficulty"
              className="problem-select"
              value={difficulty}
              onChange={(event) => onDifficulty(event.target.value)}
            >
              <option value="ALL">All</option>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
          </div>

          <div className="problem-filter">
            <label htmlFor="problem-status">STATUS</label>
            <select
              id="problem-status"
              className="problem-select"
              value={status}
              onChange={(event) => onStatus(event.target.value)}
            >
              <option value="ALL">All</option>
              <option value="SOLVED">Solved</option>
              <option value="UNSOLVED">Unsolved</option>
            </select>
          </div>

          <div className="problem-filter">
            <label htmlFor="problem-sort">SORT</label>
            <select
              id="problem-sort"
              className="problem-select"
              value={sort}
              onChange={(event) => onSort(event.target.value)}
            >
              <option value="ORDER">Default order</option>
              <option value="TITLE">Title</option>
              <option value="DIFFICULTY">Difficulty</option>
            </select>
          </div>
        </section>
      )}

      {loading ? (
        <LoadingState text="Loading problems..." />
      ) : error ? (
        <ErrorState text={error} onRetry={onRetry} />
      ) : !problems.length ? (
        <div className="empty-panel">
          <h2>No problems found.</h2>
          <p>
            Make sure the backend is running and the problem seed is loaded.
          </p>
        </div>
      ) : !filteredProblems.length ? (
        <div className="empty-panel">
          <h2>No matching problems.</h2>
          <p>Try changing the search or filters.</p>
          <button
            className="button button-small"
            onClick={() => {
              onSearch("");
              onDifficulty("ALL");
              onStatus("ALL");
              onSort("ORDER");
            }}
          >
            Clear Filters
          </button>
        </div>
      ) : (
        <div className="problem-list">
          {filteredProblems.map((problem, index) => {
            const solved = solvedProblemIds.has(problem.id);
            const difficultyClass = String(problem.difficulty || "")
              .toLowerCase();

            return (
              <article
                className={`problem-row ${solved ? "is-solved" : ""}`}
                key={problem.id}
              >
                <span className="problem-index">
                  {String(index + 1).padStart(2, "0")}
                </span>

                <div className="problem-info">
                  <div className="problem-title">
                    <h2>{problem.title}</h2>
                    <span className={`difficulty ${difficultyClass}`}>
                      {problem.difficulty}
                    </span>
                    {solved && (
                      <span className="problem-solved-badge" aria-label="Solved">
                        ✓ SOLVED
                      </span>
                    )}
                  </div>

                  <p>{problem.description}</p>

                  <small>JAVA · DSA · CODEPATH</small>
                </div>

                <button
                  className="button button-small"
                  onClick={() => onSolve(problem)}
                >
                  Solve →
                </button>
              </article>
            );
          })}
        </div>
      )}
    </PageFrame>
  );
}

/* =========================================================
   PROBLEM VIEW
========================================================= */

function ProblemView({
  user,
  problem,
  loading,
  error,
  code,
  setCode,
  copied,
  runMessage,
  executionStatus,
  executionOutput,
  testCases,
  testCasesLoading,
  testCasesError,
  submissions,
  submissionsLoading,
  submissionsError,
  submitLoading,
  submitMessage,
  onBack,
  onCopy,
  onReset,
  onRun,
  onSubmit,
  onLogin,
}) {
  if (
    loading &&
    !problem
  ) {
    return (
      <PageFrame
        title="Problem"
        kicker="CODING TERMINAL // 09"
        onBack={onBack}
      >
        <LoadingState
          text="Opening problem..."
        />
      </PageFrame>
    );
  }

  if (
    error &&
    !problem
  ) {
    return (
      <PageFrame
        title="Problem"
        kicker="CODING TERMINAL // 09"
        onBack={onBack}
      >
        <ErrorState
          text={error}
          onRetry={onBack}
        />
      </PageFrame>
    );
  }

  if (!problem) {
    return null;
  }

  return (
    <PageFrame
      title={problem.title}
      kicker="CODING TERMINAL // 09"
      onBack={onBack}
    >
      <div className="problem-workspace">

        {/* =================================================
            PROBLEM STATEMENT
        ================================================= */}

        <section className="problem-statement">

          <div className="problem-workspace-header">

            <div>
              <span className="eyebrow">
                PROBLEM
              </span>

              <h1>
                {problem.title}
              </h1>
            </div>

            <span
              className={`difficulty ${String(
                problem.difficulty
              ).toLowerCase()}`}
            >
              {
                problem.difficulty
              }
            </span>

          </div>

          <InfoBlock
            title="Description"
            text={
              problem.description
            }
          />

          <InfoBlock
            title="Examples"
            text={
              problem.examples
            }
            pre
          />

          <InfoBlock
            title="Constraints"
            text={
              problem.constraints
            }
            pre
          />

          {/* TEST CASES */}

          <section className="info-block">

            <h3>
              Test Cases
            </h3>

            {testCasesLoading ? (
              <p>
                Loading test
                cases...
              </p>
            ) : testCasesError ? (
              <div className="inline-error">
                {testCasesError}
              </div>
            ) : !testCases.length ? (
              <p>
                No test cases
                available yet.
              </p>
            ) : (
              <div className="test-case-list">

                {testCases.map(
                  (
                    testCase,
                    index
                  ) => (
                    <div
                      className="test-case-card"
                      key={
                        testCase.id ||
                        index
                      }
                    >
                      <strong>
                        Test Case{" "}
                        {index + 1}
                      </strong>

                      <div>
                        <small>
                          INPUT
                        </small>

                        <pre>
                          {
                            testCase.input
                          }
                        </pre>
                      </div>

                      <div>
                        <small>
                          EXPECTED OUTPUT
                        </small>

                        <pre>
                          {
                            testCase.expectedOutput
                          }
                        </pre>
                      </div>
                    </div>
                  )
                )}

              </div>
            )}

          </section>

        </section>

        {/* =================================================
            EDITOR
        ================================================= */}

        <section className="editor-panel">

          <div className="editor-header">

            <span>
              JAVA / MAIN.JAVA
            </span>

            <div>

              <button
                onClick={onCopy}
              >
                {copied
                  ? "Copied ✓"
                  : "Copy"}
              </button>

              <button
                onClick={onReset}
              >
                Reset
              </button>

            </div>

          </div>

          <textarea
            className="code-editor"
            value={code}
            onChange={(event) =>
              setCode(
                event.target.value
              )
            }
            spellCheck="false"
          />

          <div className="editor-actions">

            <button
              className="button button-quiet"
              onClick={onRun}
            >
              Run Code
            </button>

            {user ? (
              <button
                className="button"
                onClick={onSubmit}
                disabled={
                  submitLoading
                }
              >
                {submitLoading
                  ? "Submitting..."
                  : "Submit Solution"}
              </button>
            ) : (
              <button
                className="button"
                onClick={onLogin}
              >
                Log In to Submit
              </button>
            )}

          </div>

          {runMessage && (
            <div className="editor-message">
              {runMessage}
            </div>
          )}

          {executionStatus && (
            <div className="editor-message">
              <strong>{executionStatus}</strong>
              {executionOutput && (
                <pre style={{ margin: "8px 0 0", whiteSpace: "pre-wrap" }}>
                  {executionOutput}
                </pre>
              )}
            </div>
          )}

          {submitMessage && (
            <div className="editor-message">
              {submitMessage}
            </div>
          )}

        </section>

      </div>

      {/* =================================================
          SUBMISSION HISTORY
      ================================================= */}

      <section className="dashboard-panel submission-panel">

        <div className="panel-heading">

          <span>
            SUBMISSION HISTORY
          </span>

          {user && (
            <button
              onClick={
                loadSubmissionHistory
              }
            >
              Refresh ↻
            </button>
          )}

        </div>

        {!user ? (
          <div className="empty-panel">
            <h2>
              Log in to see your
              submissions.
            </h2>

            <p>
              Your coding history
              will appear here after
              you sign in.
            </p>
          </div>
        ) : submissionsLoading ? (
          <LoadingState
            text="Loading submissions..."
          />
        ) : submissionsError ? (
          <ErrorState
            text={
              submissionsError
            }
          />
        ) : !submissions.length ? (
          <div className="empty-panel">
            <h2>
              No submissions yet.
            </h2>

            <p>
              Submit your first
              Java solution to see
              it here.
            </p>
          </div>
        ) : (
          <div className="submission-list">

            {submissions.map(
              (submission) => (
                <div
                  className="submission-row"
                  key={
                    submission.id
                  }
                >

                  <div>
                    <strong>
                      #
                      {
                        submission.id
                      }
                    </strong>

                    <span>
                      {
                        submission.problemTitle ||
                        "Problem"
                      }
                    </span>
                  </div>

                  <div>
                    <span>
                      {
                        submission.language
                      }
                    </span>

                    <span
                      className={`submission-status ${String(
                        submission.status ||
                          "QUEUED"
                      ).toLowerCase()}`}
                    >
                      {
                        submission.status ||
                        "QUEUED"
                      }
                    </span>

                    <small>
                      {submission.createdAt
                        ? new Date(
                            submission.createdAt
                          ).toLocaleString()
                        : ""}
                    </small>
                  </div>

                </div>
              )
            )}

          </div>
        )}

      </section>
    </PageFrame>
  );

  async function loadSubmissionHistory() {
    if (!user) {
      return;
    }

    try {
      setSubmissionsLoading(true);
      setSubmissionsError("");

      const data =
        await getMySubmissions();

      setSubmissions(
        Array.isArray(data)
          ? data
          : []
      );
    } catch (submissionError) {
      setSubmissionsError(
        submissionError.message ||
          "Unable to load submissions."
      );
    } finally {
      setSubmissionsLoading(false);
    }
  }
}

/* =========================================================
   INFO BLOCK
========================================================= */

function InfoBlock({
  title,
  text,
  pre = false,
}) {
  return (
    <section className="info-block">

      <h3>
        {title}
      </h3>

      {pre ? (
        <pre>
          {text}
        </pre>
      ) : (
        <p>
          {text}
        </p>
      )}

    </section>
  );
}

/* =========================================================
   PAGE FRAME
========================================================= */

function PageFrame({
  title,
  kicker,
  onBack,
  children,
}) {
  return (
    <main className="page">

      <div className="site-container">

        <button
          className="back-button"
          onClick={onBack}
        >
          ← Back
        </button>

        <div className="page-heading">

          <span className="eyebrow">
            {kicker}
          </span>

          <h1>
            {title}
          </h1>

        </div>

        {children}

      </div>

    </main>
  );
}

/* =========================================================
   AUTH MODAL
========================================================= */

function AuthModal({
  mode,
  name,
  setName,
  email,
  setEmail,
  password,
  setPassword,
  loading,
  error,
  message,
  onClose,
  onSwitch,
  onLogin,
  onRegister,
}) {
  return (
    <div
      className="modal-layer"
      onMouseDown={(event) => {
        if (
          event.target ===
          event.currentTarget
        ) {
          onClose();
        }
      }}
    >
      <section
        className="auth-card"
        role="dialog"
        aria-modal="true"
      >

        <button
          className="modal-close"
          onClick={onClose}
          aria-label="Close"
        >
          ×
        </button>

        <span className="eyebrow">
          CODEPATH // ACCOUNT
        </span>

        <h2>
          {mode === "login"
            ? "Welcome back."
            : "Create your account."}
        </h2>

        <p>
          {mode === "login"
            ? "Continue your saved learning path."
            : "Create a free account to save your progress."}
        </p>

        <form
          onSubmit={
            mode === "login"
              ? onLogin
              : onRegister
          }
        >

          {mode === "register" && (
            <label>
              Name

              <input
                value={name}
                onChange={(event) =>
                  setName(
                    event.target.value
                  )
                }
                required
                minLength={2}
              />
            </label>
          )}

          <label>
            Email

            <input
              type="email"
              value={email}
              onChange={(event) =>
                setEmail(
                  event.target.value
                )
              }
              required
            />
          </label>

          <label>
            Password

            <input
              type="password"
              value={password}
              onChange={(event) =>
                setPassword(
                  event.target.value
                )
              }
              required
              minLength={6}
            />
          </label>

          {error && (
            <div className="form-error">
              {error}
            </div>
          )}

          {message && (
            <div className="form-success">
              {message}
            </div>
          )}

          <button
            className="button button-full"
            disabled={loading}
          >
            {loading
              ? "Please wait..."
              : mode === "login"
              ? "Log In"
              : "Create Account"}
          </button>

        </form>

        <button
          className="switch-auth"
          onClick={() =>
            onSwitch(
              mode === "login"
                ? "register"
                : "login"
            )
          }
        >
          {mode === "login"
            ? "Need an account? Create one →"
            : "Already have an account? Log in →"}
        </button>

      </section>
    </div>
  );
}

/* =========================================================
   LOADING STATE
========================================================= */

function LoadingState({
  text,
}) {
  return (
    <div className="state-panel">

      <span className="state-pixel" />

      <p>
        {text}
      </p>

    </div>
  );
}

/* =========================================================
   ERROR STATE
========================================================= */

function ErrorState({
  text,
  onRetry,
}) {
  return (
    <div className="state-panel state-error">

      <strong>
        ERROR //
      </strong>

      <p>
        {text}
      </p>

      {onRetry && (
        <button
          className="button button-small"
          onClick={onRetry}
        >
          Retry
        </button>
      )}

    </div>
  );
}

/* =========================================================
   STAT
========================================================= */

function Stat({
  label,
  value,
}) {
  return (
    <div className="stat-card">

      <span>
        {label}
      </span>

      <strong>
        {value}
      </strong>

    </div>
  );
}

/* =========================================================
   EXPORT
========================================================= */

export default App;