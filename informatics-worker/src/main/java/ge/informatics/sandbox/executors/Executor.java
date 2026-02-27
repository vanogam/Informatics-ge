package ge.informatics.sandbox.executors;

import com.github.dockerjava.api.DockerClient;
import ge.informatics.sandbox.Sandbox;
import ge.informatics.sandbox.Utils;
import ge.informatics.sandbox.model.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ge.informatics.sandbox.Utils.executeCommandSync;

public interface Executor {
    static final String MEMORY_KEY = "Maximum resident set size (kbytes)";
    static final String WALL_CLOCK_KEY = "Elapsed (wall clock) time (h:mm:ss or m:ss)";
    static final String TIME_KEY = "User time (seconds)";
    static final String START_STRING = "Command being timed:";

    String getSuffix();

    CompilationResult compileSubmission(DockerClient client, String containerId) throws IOException, InterruptedException;

    TestResult execute(DockerClient client, String containerId, Task task) throws IOException, InterruptedException;

    default TestResult buildTestResult(Task task, Utils.CommandResult result, long runtime, DockerClient client, String containerId) throws IOException, InterruptedException {
        TestResult.Builder builder = new TestResult.Builder();
        builder.withMessageType(CallbackType.TEST_COMPLETED);
        builder.withTestcaseKey(task.testId());

        if (result.isTimeout()) {
            return builder.withStatus(TestStatus.TIME_LIMIT_EXCEEDED)
                    .withSubmissionId(Long.valueOf(task.submissionId()))
                    .withTimeMillis(task.timeLimitMillis())
                    .withExitCode(0)
                    .withScore(0.0)
                    .withMessage("Execution timed out")
                    .build();
        }
        String stderr = result.getStderr().toString(StandardCharsets.UTF_8);
        if (stderr == null) {
            stderr = "";
        }
        stderr = stderr.split(START_STRING)[0].trim();
        builder.withSubmissionId(Long.valueOf(task.submissionId()));
        builder.withMessage(stderr);
        builder.withExitCode(result.getExitCode());
        Map<String, String> metrics = parseResult(result.getStderr().toString(StandardCharsets.UTF_8));
        int memory = Integer.parseInt(metrics.get(MEMORY_KEY));
        builder.withMemoryKB(memory);
        if (runtime > task.timeLimitMillis()) {
            return builder
                    .withTimeMillis(task.timeLimitMillis())
                    .withStatus(TestStatus.TIME_LIMIT_EXCEEDED)
                    .withScore(0.0)
                    .build();
        }
        builder.withTimeMillis(runtime);
        if (memory > task.memoryLimitKB()) {
            return builder
                    .withStatus(TestStatus.MEMORY_LIMIT_EXCEEDED)
                    .withScore(0.0)
                    .build();
        }

        if (result.getExitCode() != 0) {
            return builder
                    .withStatus(TestStatus.RUNTIME_ERROR)
                    .withScore(0.0)
                    .build();
        }

        evaluate(client, containerId, builder);
        // Attach contestant output snapshot (first 1000 chars) to the result
        String outcome = retrieveOutcome(client, containerId);
        builder.withOutcome(outcome);
        return builder.build();
    }

    default Map<String, String> parseResult(String result) {
        Map<String, String> metrics = new HashMap<>();
        Arrays.stream(result
                        .split("\n"))
                .forEach(val -> {
                    String[] parts = val.split(":");
                    if (parts.length == 2) {
                        metrics.put(parts[0].trim(), parts[1].trim());
                    }
                });
        return metrics;
    }


    default void evaluate(DockerClient client, String containerId, TestResult.Builder builder) throws InterruptedException {
        Utils.CommandResult result = executeCommandSync(
                client,
                containerId,
                "/usr/bin/time -v su -c '/sandbox/checker/checker /sandbox/submission/input /sandbox/checker/output /sandbox/submission/output' " + Sandbox.CHECKER_USER
        );
        if (result.isTimeout()) {
            throw new RuntimeException("Timeout during evaluation");
        }
        double score = Double.parseDouble(result.getStdout().toString(StandardCharsets.UTF_8));
        builder.withScore(score);
        if (score < 0.01) {
            builder.withStatus(TestStatus.WRONG_ANSWER);
        } else if (score > 0.99) {
            builder.withStatus(TestStatus.CORRECT);
        } else {
            builder.withStatus(TestStatus.PARTIAL);
        }
    }

    default String retrieveOutcome(DockerClient client, String containerId) throws InterruptedException {
        Utils.CommandResult result = executeCommandSync(
                client,
                containerId,
                "head -c 1000 /sandbox/submission/output"
        );
        return result.getStdout().toString(StandardCharsets.UTF_8);
    }
}
