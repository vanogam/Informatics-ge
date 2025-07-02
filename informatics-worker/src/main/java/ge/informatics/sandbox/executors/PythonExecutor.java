package ge.informatics.sandbox.executors;

import com.github.dockerjava.api.DockerClient;
import ge.informatics.sandbox.Sandbox;
import ge.informatics.sandbox.Utils;
import ge.informatics.sandbox.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static ge.informatics.sandbox.Utils.executeCommandSync;

public class PythonExecutor implements  Executor {

    private static final Logger log = LoggerFactory.getLogger(PythonExecutor.class);

    @Override
    public String getSuffix() {
        return "py";
    }

    @Override
    public CompilationResult compileSubmission(DockerClient client, String containerId) throws IOException, InterruptedException {
        log.info("Compiling python file");
        Utils.CommandResult result = executeCommandSync(client, containerId, "/usr/bin/python3 -m compileall -b .");
        if (result.getExitCode() != 0) {
            return new CompilationResult(false, result.getStderr().toString(StandardCharsets.UTF_8));
        }
        result = executeCommandSync(client, containerId, "mv /sandbox/submission/submission.pyc /sandbox/submission/submission");

        return new CompilationResult(result.getExitCode() == 0, result.getStderr().toString(StandardCharsets.UTF_8));
    }

    @Override
    public TestResult execute(DockerClient client, String containerId, Task task) throws IOException, InterruptedException {
        log.info("Executing python submission: {}", task.submissionId());
        long executionStart = System.currentTimeMillis();
        Utils.CommandResult result = executeCommandSync(
                client,
                containerId,
                "/usr/bin/time -v su -c '/usr/bin/python3 /sandbox/submission/submission' " + Sandbox.CONTESTANT_USER + " < /sandbox/submission/input > /sandbox/submission/output",
                task.timeLimitMillis() + 500,
                task.memoryLimitKB() + 10 * 1024
        );
        long runtime = System.currentTimeMillis() - executionStart;

        return buildTestResult(task, result, runtime, client, containerId);
    }
}
