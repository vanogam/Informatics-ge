package ge.informatics.sandbox.executors;

import com.github.dockerjava.api.DockerClient;
import ge.informatics.sandbox.ContainerPaths;
import ge.informatics.sandbox.Sandbox;
import ge.informatics.sandbox.Utils;
import ge.informatics.sandbox.model.CallbackType;
import ge.informatics.sandbox.model.CompilationResult;
import ge.informatics.sandbox.model.Task;
import ge.informatics.sandbox.model.TestResult;
import ge.informatics.sandbox.model.TestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static ge.informatics.sandbox.Utils.executeCommandSync;

/**
 * Runs an IOI/CMS style communication test: the manager reads the test input and talks to the
 * submission over a FIFO pair, then reports the score itself.
 *
 * <p>The manager runs as the checker user and the submission as the contestant user, so the
 * submission can neither read the manager's binary nor write its score file. Compilation is
 * delegated to the language executor, which has already linked the task's grader in.
 */
public class CommunicationExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(CommunicationExecutor.class);

    /**
     * How much longer than the submission's own limit the pair is allowed to take before being
     * killed. This is a deadlock guard, not the verdict - the verdict uses CPU time.
     */
    private static final int WALL_CLOCK_GUARD_FACTOR = 4;

    /**
     * testlib prints this on stderr when the manager itself failed, as opposed to the
     * submission being wrong. Both print a score of 0, so the message is the only way to tell.
     */
    private static final String TESTLIB_FAIL_MARKER = "FAIL";

    /**
     * Process name of the compiled manager, for pgrep/pkill -x. Matching on the name avoids the
     * self-match that a full command line pattern causes.
     */
    private static final String MANAGER_PROCESS_NAME = "manager";

    private final Executor languageExecutor;

    public CommunicationExecutor(Executor languageExecutor) {
        this.languageExecutor = languageExecutor;
    }

    @Override
    public String getSuffix() {
        return languageExecutor.getSuffix();
    }

    @Override
    public CompilationResult compileSubmission(DockerClient client, String containerId)
            throws IOException, InterruptedException {
        return languageExecutor.compileSubmission(client, containerId);
    }

    @Override
    public String runCommand(Task task) {
        return languageExecutor.runCommand(task);
    }

    @Override
    public TestResult execute(DockerClient client, String containerId, Task task)
            throws IOException, InterruptedException {
        if (task.processCount() != 1) {
            throw new UnsupportedOperationException(
                    "Communication tasks with " + task.processCount() + " solution processes are not supported yet");
        }
        String solutionToManager = ContainerPaths.fifoSolutionToManager(0);
        String managerToSolution = ContainerPaths.fifoManagerToSolution(0);

        try {
            createFifos(client, containerId, solutionToManager, managerToSolution);
            startManager(client, containerId, task, solutionToManager, managerToSolution);

            long executionStart = System.currentTimeMillis();
            Utils.CommandResult solutionResult = runSolution(client, containerId, task,
                    solutionToManager, managerToSolution);
            long runtime = System.currentTimeMillis() - executionStart;

            waitForManager(client, containerId, task);
            return buildTestResult(task, solutionResult, runtime, client, containerId);
        } finally {
            cleanUp(client, containerId);
        }
    }

    private void createFifos(DockerClient client, String containerId, String... fifos) throws InterruptedException {
        StringBuilder command = new StringBuilder("mkdir -p " + ContainerPaths.FIFO_DIR);
        for (String fifo : fifos) {
            command.append(" && rm -f ").append(fifo).append(" && mkfifo -m 660 ").append(fifo);
        }
        // The directory belongs to the contestant and the checker user is in that group, so
        // both processes can open both ends without either gaining access to the other's files.
        command.append(" && chown -R ").append(Sandbox.CONTESTANT_USER).append(":")
                .append(Sandbox.CONTESTANT_USER).append(" ").append(ContainerPaths.FIFO_DIR);
        Utils.CommandResult result = executeCommandSync(client, containerId, command.toString());
        if (result.getExitCode() != 0) {
            throw new RuntimeException("Could not create FIFOs: "
                    + result.getStderr().toString(StandardCharsets.UTF_8));
        }
    }

    /**
     * Launches the manager detached, so it is already waiting when the submission opens its end.
     * Opening a FIFO blocks until the other side is present, so the order matters.
     */
    private void startManager(DockerClient client, String containerId, Task task,
                              String solutionToManager, String managerToSolution) throws InterruptedException {
        String command = "nohup su -c '" + ContainerPaths.managerBinary()
                + " " + solutionToManager + " " + managerToSolution
                + " < " + ContainerPaths.submissionInput()
                + " > " + ContainerPaths.managerScore()
                + " 2> " + ContainerPaths.managerMessage() + "' " + Sandbox.CHECKER_USER + " &";
        executeCommandSync(client, containerId,
                "rm -f " + ContainerPaths.managerScore() + " " + ContainerPaths.managerMessage()
                        + " && touch " + ContainerPaths.managerScore() + " " + ContainerPaths.managerMessage()
                        + " && chown " + Sandbox.CHECKER_USER + ":" + Sandbox.CHECKER_USER
                        + " " + ContainerPaths.managerScore() + " " + ContainerPaths.managerMessage()
                        + " && " + command);
        log.debug("Manager started for task {} test {}", task.taskId(), task.testId());
    }

    private Utils.CommandResult runSolution(DockerClient client, String containerId, Task task,
                                            String solutionToManager, String managerToSolution)
            throws InterruptedException {
        long guardMillis = task.timeLimitMillis() * WALL_CLOCK_GUARD_FACTOR;
        String command = "/usr/bin/time -v su -c '" + languageExecutor.runCommand(task) + "' "
                + Sandbox.CONTESTANT_USER
                + " < " + managerToSolution + " > " + solutionToManager;
        return Utils.executeGuarded(client, containerId, command,
                guardMillis + 1000, task.memoryLimitKB() + 10 * 1024, guardMillis);
    }

    /**
     * Gives the manager a moment to finish scoring after the submission exits, then makes sure
     * it is gone so it cannot bleed into the next test.
     */
    private void waitForManager(DockerClient client, String containerId, Task task) throws InterruptedException {
        long deadline = System.currentTimeMillis() + Math.min(5000, task.timeLimitMillis() + 2000);
        while (System.currentTimeMillis() < deadline) {
            // Match the process name, not the command line: a -f pattern would also match the
            // shell running this very check, so the manager would never look finished.
            Utils.CommandResult running = executeCommandSync(client, containerId,
                    "pgrep -x " + MANAGER_PROCESS_NAME + " > /dev/null && echo running");
            if (!running.getStdout().toString(StandardCharsets.UTF_8).trim().equals("running")) {
                return;
            }
            Thread.sleep(50);
        }
        log.warn("Manager for task {} test {} outlived the submission, killing it", task.taskId(), task.testId());
        executeCommandSync(client, containerId, "pkill -x " + MANAGER_PROCESS_NAME);
    }

    private void cleanUp(DockerClient client, String containerId) {
        try {
            executeCommandSync(client, containerId,
                    "pkill -x " + MANAGER_PROCESS_NAME + "; rm -f " + ContainerPaths.FIFO_DIR + "/*");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Takes the verdict from the manager instead of running a checker afterwards.
     */
    @Override
    public void evaluate(DockerClient client, String containerId, TestResult.Builder builder)
            throws InterruptedException {
        String scoreText = readFileAsChecker(client, containerId, ContainerPaths.managerScore()).trim();
        String message = readFileAsChecker(client, containerId, ContainerPaths.managerMessage()).trim();

        if (scoreText.isEmpty()) {
            throw new RuntimeException("Manager produced no score. Manager output: " + message);
        }
        // testlib in checker mode prints the score on the first line and the reason on stderr.
        String firstLine = scoreText.split("\\R", 2)[0].trim();
        double score;
        try {
            score = Double.parseDouble(firstLine);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Manager produced an unreadable score '" + firstLine
                    + "'. Manager output: " + message);
        }

        if (score <= 0 && message.contains(TESTLIB_FAIL_MARKER)) {
            // A broken manager must page the teacher, not silently fail the contestant.
            throw new RuntimeException("Manager reported a failure: " + message);
        }

        applyScore(builder, score);
        if (!message.isEmpty()) {
            builder.withMessage(message);
        }
    }

    /**
     * The manager's score and message belong to the checker user; the worker reads them through
     * the container rather than granting the contestant any access.
     */
    private String readFileAsChecker(DockerClient client, String containerId, String path)
            throws InterruptedException {
        Utils.CommandResult result = executeCommandSync(client, containerId, "head -c 4000 " + path);
        return result.getStdout().toString(StandardCharsets.UTF_8);
    }

    /**
     * The submission communicates over FIFOs rather than writing an output file, so there is no
     * contestant output to attach - the manager's message is the useful feedback.
     */
    @Override
    public String retrieveOutcome(DockerClient client, String containerId) {
        return "";
    }
}
