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

import java.io.ByteArrayOutputStream;
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

    /** Separates the per-process blocks when their stderr files are dumped in one exec. */
    private static final String PROCESS_MARKER = "###process ";

    /** What `timeout` reports when it kills the command it was guarding. */
    private static final int TIMEOUT_EXIT_CODE = 124;

    /** Stand-in status for a process that left no exit status behind at all. */
    private static final int KILLED_EXIT_CODE = 137;

    private static final int MAX_PROCESS_MESSAGE_CHARS = 500;

    private static final int MAX_MESSAGE_CHARS = 4000;

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
        int processes = task.processCount();
        try {
            createFifos(client, containerId, processes);
            startManager(client, containerId, task, processes);

            long executionStart = System.currentTimeMillis();
            Utils.CommandResult solutionResult = runSolutions(client, containerId, task, processes);
            long runtime = System.currentTimeMillis() - executionStart;

            waitForManager(client, containerId, task);
            return buildTestResult(task, solutionResult, runtime, client, containerId);
        } finally {
            cleanUp(client, containerId);
        }
    }

    /**
     * One FIFO pair per solution process. All of them are created up front, because the manager
     * opens every pair before it starts talking to any process.
     */
    private void createFifos(DockerClient client, String containerId, int processes) throws InterruptedException {
        StringBuilder command = new StringBuilder("mkdir -p " + ContainerPaths.FIFO_DIR);
        for (int i = 0; i < processes; i++) {
            for (String fifo : new String[]{ContainerPaths.fifoSolutionToManager(i),
                    ContainerPaths.fifoManagerToSolution(i)}) {
                command.append(" && rm -f ").append(fifo).append(" && mkfifo -m 660 ").append(fifo);
            }
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
     * Launches the manager detached, so it is already waiting when the submissions open their
     * ends. Opening a FIFO blocks until the other side is present, so the order matters.
     *
     * <p>The manager receives one pair of FIFO paths per process, solution-to-manager first -
     * the CMS argument order, which is what task-supplied managers are written against. A
     * manager infers the process count from how many pairs it is handed.
     */
    private void startManager(DockerClient client, String containerId, Task task, int processes)
            throws InterruptedException {
        StringBuilder fifoArgs = new StringBuilder();
        for (int i = 0; i < processes; i++) {
            fifoArgs.append(' ').append(ContainerPaths.fifoSolutionToManager(i))
                    .append(' ').append(ContainerPaths.fifoManagerToSolution(i));
        }
        String command = "nohup su -c '" + ContainerPaths.managerBinary()
                + fifoArgs
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

    /**
     * Starts every solution process at once and waits for all of them. They have to run
     * concurrently: the manager holds all the FIFO pairs open and decides for itself which
     * process it talks to next, so a process that has not started yet would deadlock it.
     *
     * <p>Each process gets its own FIFO pair as stdin/stdout and, when the task uses more than
     * one, its index as the single argument - again the CMS convention the task's stub expects.
     */
    private Utils.CommandResult runSolutions(DockerClient client, String containerId, Task task, int processes)
            throws InterruptedException {
        long guardMillis = wallClockGuardMillis(task, processes);
        // Mirrors executeGuarded: the ceiling is doubled for interpreter and runtime overhead,
        // and applies per process because ulimit is inherited, not shared.
        long memoryCeilingKB = (task.memoryLimitKB() + 10L * 1024) * 2;
        StringBuilder script = new StringBuilder("mkdir -p " + ContainerPaths.RUN_DIR
                + " && rm -f " + ContainerPaths.RUN_DIR + "/*"
                + " && ulimit -v " + memoryCeilingKB + " ; ");
        for (int i = 0; i < processes; i++) {
            // The exit status is written to a file: these run detached, so `wait` cannot report
            // which of them failed. Note there is no separator between the jobs - `&` is one,
            // and a `;` after it is a syntax error.
            script.append("( timeout ").append(guardMillis / 1000f).append("s /usr/bin/time -v su -c '")
                    .append(solutionCommand(task, processes, i)).append("' ").append(Sandbox.CONTESTANT_USER)
                    .append(" < ").append(ContainerPaths.fifoManagerToSolution(i))
                    .append(" > ").append(ContainerPaths.fifoSolutionToManager(i))
                    .append(" 2> ").append(ContainerPaths.processStderr(i))
                    .append(" ; echo $? > ").append(ContainerPaths.processExitCode(i))
                    .append(" ) & ");
        }
        script.append("wait");
        Utils.CommandResult launch = executeCommandSync(client, containerId, script.toString(),
                guardMillis + 2000, null);
        if (launch.isTimeout()) {
            return launch;
        }
        return collectSolutionResults(client, containerId, processes);
    }

    /**
     * Wall-clock budget for a single process. This is only a deadlock guard - the verdict comes
     * from CPU time - so it scales with the process count: the manager drives the processes one
     * at a time, which means a process spends most of its wall clock blocked waiting its turn
     * while the others work.
     */
    private long wallClockGuardMillis(Task task, int processes) {
        return task.timeLimitMillis() * WALL_CLOCK_GUARD_FACTOR * processes;
    }

    private String solutionCommand(Task task, int processes, int index) {
        String command = languageExecutor.runCommand(task);
        return processes == 1 ? command : command + " " + index;
    }

    /**
     * Folds the per-process stderr files and exit statuses into the single result the shared
     * verdict logic expects: the worst exit status, the largest memory footprint and the
     * longest CPU time of any process, since one process breaking a limit fails the test.
     */
    private Utils.CommandResult collectSolutionResults(DockerClient client, String containerId, int processes)
            throws InterruptedException {
        StringBuilder dump = new StringBuilder();
        for (int i = 0; i < processes; i++) {
            dump.append("echo \"").append(PROCESS_MARKER).append(i).append(' ')
                    .append("$(cat ").append(ContainerPaths.processExitCode(i)).append(" 2>/dev/null)\" ; ")
                    .append("cat ").append(ContainerPaths.processStderr(i)).append(" 2>/dev/null ; ");
        }
        Utils.CommandResult dumped = executeCommandSync(client, containerId, dump.toString());
        if (dumped.isTimeout()) {
            return dumped;
        }

        int exitCode = 0;
        long maxCpuMillis = 0;
        int maxMemoryKB = 0;
        StringBuilder messages = new StringBuilder();
        for (String block : dumped.getStdout().toString(StandardCharsets.UTF_8).split(PROCESS_MARKER)) {
            if (block.isBlank()) {
                continue;
            }
            // "<index> <exit status>\n<stderr of that process>"
            String header = block.split("\\R", 2)[0].trim();
            String stderr = block.substring(Math.min(block.length(), header.length() + 1));
            String[] headerParts = header.split("\\s+");
            int index = parseIntOrDefault(headerParts[0], -1);
            // A missing status means the process never got to write one - killed outright.
            int processExit = headerParts.length > 1 ? parseIntOrDefault(headerParts[1], KILLED_EXIT_CODE)
                    : KILLED_EXIT_CODE;
            if (processExit == TIMEOUT_EXIT_CODE) {
                // The guard fired. Reported as a timeout so the verdict is a time limit rather
                // than a runtime error, exactly as the single-process guard in executeGuarded is.
                log.warn("Solution process {} was killed by the wall clock guard", index);
                return new Utils.CommandResult(true);
            }
            if (processExit != 0 && exitCode == 0) {
                exitCode = processExit;
            }

            Map<String, String> metrics = parseResult(stderr);
            maxCpuMillis = Math.max(maxCpuMillis, cpuTimeMillis(metrics, 0));
            maxMemoryKB = Math.max(maxMemoryKB, parseIntOrDefault(metrics.get(MEMORY_KEY), 0));
            appendMessage(messages, processes, index, stderr.split(START_STRING)[0].trim());
        }
        return synthesizeResult(exitCode, messages.toString(), maxCpuMillis, maxMemoryKB);
    }

    /**
     * Labels each process's diagnostics with its index, so a contestant reading the feedback can
     * tell which of them failed, and caps the total - 64 crashing processes would otherwise fill
     * the result with the same stack trace repeated.
     */
    private void appendMessage(StringBuilder messages, int processes, int index, String stderr) {
        if (stderr.isEmpty() || messages.length() >= MAX_MESSAGE_CHARS) {
            return;
        }
        if (processes > 1) {
            messages.append("[process ").append(index).append("] ");
        }
        messages.append(stderr.length() > MAX_PROCESS_MESSAGE_CHARS
                        ? stderr.substring(0, MAX_PROCESS_MESSAGE_CHARS) + "..."
                        : stderr)
                .append('\n');
    }

    /**
     * Rebuilds the stderr of a single timed run from the aggregated numbers, so the shared
     * verdict logic can parse limits out of it the same way it does for a one-process test.
     */
    private Utils.CommandResult synthesizeResult(int exitCode, String messages, long cpuMillis, int memoryKB) {
        String stderr = messages
                + START_STRING + " \"solution processes\"\n"
                + "\t" + TIME_KEY + ": " + (cpuMillis / 1000f) + "\n"
                + "\t" + SYSTEM_TIME_KEY + ": 0.0\n"
                + "\t" + MEMORY_KEY + ": " + memoryKB + "\n";
        ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();
        stderrStream.writeBytes(stderr.getBytes(StandardCharsets.UTF_8));
        return new Utils.CommandResult(exitCode, new ByteArrayOutputStream(), stderrStream);
    }

    private int parseIntOrDefault(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
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
