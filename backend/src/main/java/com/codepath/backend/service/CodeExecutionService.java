package com.codepath.backend.service;

import com.codepath.backend.model.Problem;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeExecutionService {

    private static final String DOCKER_IMAGE = "eclipse-temurin:21-jdk";

    private static final int TIME_LIMIT_SECONDS = 7;
    private static final int OUTPUT_LIMIT = 10000;
    private static final int CODE_LIMIT = 50000;

    public ExecutionResult executeProblem(
            Problem problem,
            String code,
            String input
    ) {
        if (problem == null) {
            return new ExecutionResult(
                    "SYSTEM_ERROR",
                    "Problem configuration is missing."
            );
        }

        if (code == null || code.isBlank()) {
            return new ExecutionResult(
                    "COMPILATION_ERROR",
                    "Code cannot be empty."
            );
        }

        if (code.length() > CODE_LIMIT) {
            return new ExecutionResult(
                    "COMPILATION_ERROR",
                    "Code exceeds the 50,000 character limit."
            );
        }

        try {
            String source = buildHarness(
                    problem,
                    code,
                    input == null ? "" : input
            );

            return executeSource(source);

        } catch (IllegalArgumentException exception) {
            return new ExecutionResult(
                    "SYSTEM_ERROR",
                    exception.getMessage()
            );
        }
    }

    /*
     * Kept for the simple execution endpoint. It now expects a complete
     * Main.java-style program, so the endpoint can still be used for
     * controlled internal testing.
     */
    public ExecutionResult executeJava(String code, String input) {
        if (code == null || code.isBlank()) {
            return new ExecutionResult(
                    "COMPILATION_ERROR",
                    "Code cannot be empty."
            );
        }

        if (code.length() > CODE_LIMIT) {
            return new ExecutionResult(
                    "COMPILATION_ERROR",
                    "Code exceeds the 50,000 character limit."
            );
        }

        try {
            return executeSource(code);
        } catch (Exception exception) {
            return new ExecutionResult(
                    "SYSTEM_ERROR",
                    exception.getMessage() == null
                            ? "Code execution failed."
                            : exception.getMessage()
            );
        }
    }

    private ExecutionResult executeSource(String source) {
        Path tempDirectory = null;
        ExecutorService outputExecutor = null;
        Process process = null;

        try {
            tempDirectory = Files.createTempDirectory("codepath-");

            Path javaFile = tempDirectory.resolve("Main.java");

            Files.writeString(
                    javaFile,
                    source,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            List<String> command = new ArrayList<>();

            command.add("docker");
            command.add("run");
            command.add("--rm");

            // No network access from submitted code.
            command.add("--network");
            command.add("none");

            // Resource limits.
            command.add("--memory");
            command.add("256m");

            command.add("--cpus");
            command.add("1");

            command.add("--pids-limit");
            command.add("64");

            // Security restrictions.
            command.add("--cap-drop");
            command.add("ALL");

            command.add("--security-opt");
            command.add("no-new-privileges");

            // Read-only container filesystem.
            command.add("--read-only");

            // Small writable /tmp, without executable files.
            command.add("--tmpfs");
            command.add("/tmp:rw,noexec,nosuid,size=64m");

            // Only the temporary application directory is writable.
            command.add("-v");
            command.add(
                    tempDirectory.toAbsolutePath() + ":/app:rw"
            );

            command.add("-w");
            command.add("/app");

            command.add(DOCKER_IMAGE);
            command.add("sh");
            command.add("-c");
            command.add("javac Main.java && java Main");

            ProcessBuilder processBuilder =
                    new ProcessBuilder(command);

            // Merge stderr into stdout so both compilation and runtime
            // errors are captured by the same bounded reader.
            processBuilder.redirectErrorStream(true);

            process = processBuilder.start();

            final Process runningProcess = process;
            final AtomicBoolean outputLimitExceeded =
                    new AtomicBoolean(false);

            /*
             * Read process output on a separate thread. This is critical:
             * the main thread must be able to enforce the execution timeout
             * even when the submitted program never closes its output stream.
             */
            outputExecutor = Executors.newSingleThreadExecutor();

            Future<String> outputFuture =
                    outputExecutor.submit(() -> {

                        StringBuilder output = new StringBuilder();

                        try (
                                BufferedReader reader =
                                        new BufferedReader(
                                                new InputStreamReader(
                                                        runningProcess
                                                                .getInputStream(),
                                                        StandardCharsets.UTF_8
                                                )
                                        )
                        ) {

                            char[] buffer = new char[1024];
                            int charsRead;

                            while (
                                    (charsRead = reader.read(buffer)) != -1
                            ) {

                                output.append(
                                        buffer,
                                        0,
                                        charsRead
                                );

                                if (output.length() > OUTPUT_LIMIT) {
                                    outputLimitExceeded.set(true);
                                    runningProcess.destroyForcibly();
                                    break;
                                }
                            }

                        } catch (Exception ignored) {
                            // The process may be forcibly terminated because
                            // of timeout or output limit.
                        }

                        return output.toString();
                    });

            /*
             * Start the timeout immediately after the Docker process starts.
             * Do NOT wait for stdout to close before enforcing the timeout.
             */
            boolean finished = process.waitFor(
                    TIME_LIMIT_SECONDS,
                    TimeUnit.SECONDS
            );

            if (!finished) {
                process.destroyForcibly();

                // Give the Docker CLI a short window to terminate cleanly.
                process.waitFor(2, TimeUnit.SECONDS);

                return new ExecutionResult(
                        "TIME_LIMIT_EXCEEDED",
                        "Execution exceeded the 7 second time limit."
                );
            }

            String result;

            try {
                result = outputFuture.get(2, TimeUnit.SECONDS);
            } catch (Exception exception) {
                result = "";
            }

            if (outputLimitExceeded.get()) {
                return new ExecutionResult(
                        "RUNTIME_ERROR",
                        "Output exceeded the 10,000 character limit."
                );
            }

            int exitCode = process.exitValue();
            result = result.trim();

            if (exitCode == 0) {
                return new ExecutionResult(
                        "SUCCESS",
                        result
                );
            }

            if (looksLikeCompilationError(result)) {
                return new ExecutionResult(
                        "COMPILATION_ERROR",
                        result
                );
            }

            return new ExecutionResult(
                    "RUNTIME_ERROR",
                    result.isBlank()
                            ? "Program terminated with exit code " + exitCode + "."
                            : result
            );

        } catch (Exception exception) {

            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }

            return new ExecutionResult(
                    "SYSTEM_ERROR",
                    exception.getMessage() == null
                            ? "Code execution failed."
                            : exception.getMessage()
            );

        } finally {

            if (outputExecutor != null) {
                outputExecutor.shutdownNow();
            }

            if (tempDirectory != null) {
                deleteDirectory(tempDirectory);
            }
        }
    }

    private String buildHarness(
            Problem problem,
            String userCode,
            String rawInput
    ) {
        String judgeType = problem.getJudgeType();

        if (judgeType == null || judgeType.isBlank()) {
            throw new IllegalArgumentException(
                    "This problem has no judge configuration yet."
            );
        }

        String methodName = problem.getMethodName();

        if (methodName == null || methodName.isBlank()) {
            throw new IllegalArgumentException(
                    "This problem has no method configuration yet."
            );
        }

        String normalizedUserCode =
                normalizeUserCode(userCode);

        String harness;

        switch (judgeType) {

            case "INT_ARRAY_INT_TO_INT_ARRAY":
                harness = buildTwoSumHarness(
                        methodName,
                        rawInput
                );
                break;

            case "INT_ARRAY_TO_VOID":
                harness = buildIntArrayVoidHarness(
                        methodName,
                        rawInput
                );
                break;

            case "INT_ARRAY_TO_INT":
                harness = buildIntArrayIntHarness(
                        methodName,
                        rawInput
                );
                break;

            case "INT_ARRAY_INT_TO_INT":
                harness = buildBinarySearchHarness(
                        methodName,
                        rawInput
                );
                break;

            case "STRING_TO_BOOLEAN":
                harness = buildStringBooleanHarness(
                        methodName,
                        rawInput
                );
                break;

            case "LIST_NODE_TO_LIST_NODE":
                harness = buildLinkedListHarness(
                        methodName,
                        rawInput
                );
                break;

            case "INT_MATRIX_TO_INT_MATRIX":
                harness = buildIntMatrixHarness(
                        methodName,
                        rawInput
                );
                break;

            case "CHAR_MATRIX_TO_INT":
                harness = buildCharMatrixHarness(
                        methodName,
                        rawInput
                );
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported judge type: " + judgeType
                );
        }

        return normalizedUserCode
                + System.lineSeparator()
                + System.lineSeparator()
                + harness;
    }

    private String normalizeUserCode(String code) {
        String normalized = code
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();

        /*
         * Main.java may only contain one public top-level class.
         * The platform expects the learner's Solution to be non-public.
         */
        normalized = normalized.replaceFirst(
                "\\bpublic\\s+class\\s+Solution\\b",
                "class Solution"
        );

        return normalized;
    }

    private String buildTwoSumHarness(
            String methodName,
            String input
    ) {
        int[] nums = parseIntArray(input);
        int target = parseTarget(input);

        return """
                class Main {
                    public static void main(String[] args) {
                        int[] nums = %s;
                        int target = %d;

                        Solution solution = new Solution();
                        int[] result = solution.%s(nums, target);

                        System.out.println(formatIntArray(result));
                    }

                    private static String formatIntArray(int[] values) {
                        if (values == null) {
                            return "null";
                        }

                        StringBuilder builder = new StringBuilder("[");
                        for (int i = 0; i < values.length; i++) {
                            if (i > 0) {
                                builder.append(",");
                            }
                            builder.append(values[i]);
                        }
                        return builder.append("]").toString();
                    }
                }
                """.formatted(
                intArrayLiteral(nums),
                target,
                methodName
        );
    }

    private String buildIntArrayVoidHarness(
            String methodName,
            String input
    ) {
        int[] nums = parseIntArray(input);

        return """
                class Main {
                    public static void main(String[] args) {
                        int[] nums = %s;

                        Solution solution = new Solution();
                        solution.%s(nums);

                        System.out.println(formatIntArray(nums));
                    }

                    private static String formatIntArray(int[] values) {
                        if (values == null) {
                            return "null";
                        }

                        StringBuilder builder = new StringBuilder("[");
                        for (int i = 0; i < values.length; i++) {
                            if (i > 0) {
                                builder.append(",");
                            }
                            builder.append(values[i]);
                        }
                        return builder.append("]").toString();
                    }
                }
                """.formatted(
                intArrayLiteral(nums),
                methodName
        );
    }

    private String buildIntArrayIntHarness(
            String methodName,
            String input
    ) {
        int[] nums = parseIntArray(input);

        return """
                class Main {
                    public static void main(String[] args) {
                        int[] nums = %s;

                        Solution solution = new Solution();
                        int result = solution.%s(nums);

                        System.out.println(result);
                    }
                }
                """.formatted(
                intArrayLiteral(nums),
                methodName
        );
    }

    private String buildBinarySearchHarness(
            String methodName,
            String input
    ) {
        int[] nums = parseIntArray(input);
        int target = parseTarget(input);

        return """
                class Main {
                    public static void main(String[] args) {
                        int[] nums = %s;
                        int target = %d;

                        Solution solution = new Solution();
                        int result = solution.%s(nums, target);

                        System.out.println(result);
                    }
                }
                """.formatted(
                intArrayLiteral(nums),
                target,
                methodName
        );
    }

    private String buildStringBooleanHarness(
            String methodName,
            String input
    ) {
        String value = parseQuotedString(input);

        return """
                class Main {
                    public static void main(String[] args) {
                        String value = %s;

                        Solution solution = new Solution();
                        boolean result = solution.%s(value);

                        System.out.println(result);
                    }
                }
                """.formatted(
                javaStringLiteral(value),
                methodName
        );
    }

    private String buildLinkedListHarness(
            String methodName,
            String input
    ) {
        int[] values = parseLinkedList(input);

        return """
                class Main {
                    public static void main(String[] args) {
                        Solution.ListNode head = buildList(%s);

                        Solution solution = new Solution();
                        Solution.ListNode result =
                                solution.%s(head);

                        System.out.println(formatList(result));
                    }

                    private static Solution.ListNode buildList(int[] values) {
                        Solution.ListNode dummy =
                                new Solution.ListNode(0);

                        Solution.ListNode current = dummy;

                        for (int value : values) {
                            current.next =
                                    new Solution.ListNode(value);
                            current = current.next;
                        }

                        return dummy.next;
                    }

                    private static String formatList(
                            Solution.ListNode head
                    ) {
                        StringBuilder builder =
                                new StringBuilder();

                        Solution.ListNode current = head;

                        while (current != null) {
                            if (builder.length() > 0) {
                                builder.append(" -> ");
                            }

                            builder.append(current.val);
                            current = current.next;
                        }

                        return builder.toString();
                    }
                }
                """.formatted(
                intArrayLiteral(values),
                methodName
        );
    }

    private String buildIntMatrixHarness(
            String methodName,
            String input
    ) {
        int[][] matrix = parseIntMatrix(input);

        return """
                import java.util.Arrays;

                class Main {
                    public static void main(String[] args) {
                        int[][] intervals = %s;

                        Solution solution = new Solution();
                        int[][] result =
                                solution.%s(intervals);

                        System.out.println(formatMatrix(result));
                    }

                    private static String formatMatrix(
                            int[][] matrix
                    ) {
                        if (matrix == null) {
                            return "null";
                        }

                        StringBuilder builder =
                                new StringBuilder("[");

                        for (int i = 0; i < matrix.length; i++) {
                            if (i > 0) {
                                builder.append(",");
                            }

                            builder.append(
                                    Arrays.toString(matrix[i])
                            );
                        }

                        return builder.append("]").toString()
                                .replace(" ", "");
                    }
                }
                """.formatted(
                intMatrixLiteral(matrix),
                methodName
        );
    }

    private String buildCharMatrixHarness(
            String methodName,
            String input
    ) {
        char[][] grid = parseCharMatrix(input);

        return """
                class Main {
                    public static void main(String[] args) {
                        char[][] grid = %s;

                        Solution solution = new Solution();
                        int result =
                                solution.%s(grid);

                        System.out.println(result);
                    }
                }
                """.formatted(
                charMatrixLiteral(grid),
                methodName
        );
    }

    private int[] parseIntArray(String input) {
        Matcher matcher = Pattern
                .compile("\\[([^\\[\\]]*)\\]")
                .matcher(input);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Could not parse integer array input: " + input
            );
        }

        String body = matcher.group(1).trim();

        if (body.isBlank()) {
            return new int[0];
        }

        String[] parts = body.split(",");

        int[] values = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            values[i] = Integer.parseInt(
                    parts[i].trim()
            );
        }

        return values;
    }

    private int parseTarget(String input) {
        Matcher matcher = Pattern
                .compile(
                        "target\\s*=\\s*(-?\\d+)",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(input);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Could not parse target from input: " + input
            );
        }

        return Integer.parseInt(matcher.group(1));
    }

    private String parseQuotedString(String input) {
        Matcher matcher = Pattern
                .compile("\"((?:\\\\.|[^\"\\\\])*)\"")
                .matcher(input);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Could not parse quoted string input: " + input
            );
        }

        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private int[] parseLinkedList(String input) {
        if (input.contains("->")) {
            String[] parts = input.split("->");
            int[] values = new int[parts.length];

            for (int i = 0; i < parts.length; i++) {
                values[i] = Integer.parseInt(
                        parts[i].trim()
                );
            }

            return values;
        }

        return parseIntArray(input);
    }

    private int[][] parseIntMatrix(String input) {
        List<int[]> rows = new ArrayList<>();

        Matcher matcher = Pattern
                .compile("\\[([^\\[\\]]*)\\]")
                .matcher(input);

        while (matcher.find()) {
            String body = matcher.group(1).trim();

            if (body.isBlank()) {
                rows.add(new int[0]);
                continue;
            }

            String[] parts = body.split(",");

            int[] row = new int[parts.length];

            for (int i = 0; i < parts.length; i++) {
                row[i] = Integer.parseInt(
                        parts[i].trim()
                );
            }

            rows.add(row);
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Could not parse integer matrix input."
            );
        }

        return rows.toArray(new int[0][]);
    }

    private char[][] parseCharMatrix(String input) {
        List<char[]> rows = new ArrayList<>();

        Pattern rowPattern =
                Pattern.compile("\\[([^\\[\\]]*)\\]");

        Matcher matcher = rowPattern.matcher(input);

        while (matcher.find()) {
            String body = matcher.group(1);

            Matcher quoted =
                    Pattern.compile("\"([^\"]*)\"")
                            .matcher(body);

            List<Character> chars = new ArrayList<>();

            while (quoted.find()) {
                String value = quoted.group(1);

                if (value.length() != 1) {
                    throw new IllegalArgumentException(
                            "Each grid cell must contain one character."
                    );
                }

                chars.add(value.charAt(0));
            }

            if (!chars.isEmpty()) {
                char[] row = new char[chars.size()];

                for (int i = 0; i < chars.size(); i++) {
                    row[i] = chars.get(i);
                }

                rows.add(row);
            }
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "Could not parse character matrix input."
            );
        }

        return rows.toArray(new char[0][]);
    }

    private String intArrayLiteral(int[] values) {
        StringBuilder builder =
                new StringBuilder("new int[]{");

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(values[i]);
        }

        return builder.append("}").toString();
    }

    private String intMatrixLiteral(int[][] matrix) {
        StringBuilder builder =
                new StringBuilder("new int[][]{");

        for (int i = 0; i < matrix.length; i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(intArrayLiteral(matrix[i]));
        }

        return builder.append("}").toString();
    }

    private String charMatrixLiteral(char[][] matrix) {
        StringBuilder builder =
                new StringBuilder("new char[][]{");

        for (int i = 0; i < matrix.length; i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append("new char[]{");

            for (int j = 0; j < matrix[i].length; j++) {
                if (j > 0) {
                    builder.append(",");
                }

                builder.append(
                        javaCharLiteral(matrix[i][j])
                );
            }

            builder.append("}");
        }

        return builder.append("}").toString();
    }

    private String javaStringLiteral(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                + "\"";
    }

    private String javaCharLiteral(char value) {
        if (value == '\\') {
            return "'\\\\'";
        }

        if (value == '\'') {
            return "'\\''";
        }

        return "'" + value + "'";
    }

    private boolean looksLikeCompilationError(
            String output
    ) {
        return output.contains("error:")
                || output.contains("cannot find symbol")
                || output.contains("class, interface, enum")
                || output.contains("illegal start")
                || output.contains("reached end of file");
    }

    private void deleteDirectory(Path directory) {
        try {
            if (!Files.exists(directory)) {
                return;
            }

            Files.walk(directory)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });

        } catch (Exception ignored) {
        }
    }

    public record ExecutionResult(
            String status,
            String output
    ) {
    }
}
