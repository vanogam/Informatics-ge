package ge.informatics.sandbox.executors;

import com.github.dockerjava.api.DockerClient;
import ge.informatics.sandbox.model.CallbackType;
import ge.informatics.sandbox.model.CompilationResult;
import ge.informatics.sandbox.model.Task;
import ge.informatics.sandbox.model.TestResult;

import java.io.IOException;

/**
 * Judges a test of an output-only submission: the contestant uploaded the answer itself, so there
 * is nothing to compile and nothing to run.
 *
 * <p>The sandbox has already put their file where a program's output would have gone, which is
 * what lets the whole evaluation pipeline stay the same - including a task-supplied checker, which
 * is the only thing that can decide whether an answer is right for tasks scored this way.
 *
 * <p>Time and memory are reported as zero rather than left out: the contestant spent none of the
 * limits here, and a limit that was never consumed cannot be exceeded.
 */
public class OutputOnlyExecutor implements Executor {

    @Override
    public String getSuffix() {
        return "out";
    }

    @Override
    public CompilationResult compileSubmission(DockerClient client, String containerId) {
        throw new UnsupportedOperationException("An output-only submission is never compiled");
    }

    @Override
    public String runCommand(Task task) {
        throw new UnsupportedOperationException("An output-only submission is never run");
    }

    @Override
    public TestResult execute(DockerClient client, String containerId, Task task)
            throws IOException, InterruptedException {
        TestResult.Builder builder = new TestResult.Builder()
                .withMessageType(CallbackType.TEST_COMPLETED)
                .withTestcaseKey(task.testId())
                .withSubmissionId(Long.valueOf(task.submissionId()))
                .withExitCode(0)
                .withTimeMillis(0)
                .withMemoryKB(0);

        evaluateOrSystemError(client, containerId, builder);
        builder.withOutcome(retrieveOutcome(client, containerId));
        return builder.build();
    }
}
