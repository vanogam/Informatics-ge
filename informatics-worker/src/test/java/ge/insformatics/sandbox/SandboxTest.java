package ge.insformatics.sandbox;


import ge.informatics.sandbox.Config;
import ge.informatics.sandbox.model.*;
import ge.informatics.sandbox.Sandbox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.dockerjava.api.DockerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

import static ge.informatics.sandbox.Utils.compressFile;
import static ge.informatics.sandbox.Utils.createDockerClient;
import static org.junit.jupiter.api.Assertions.*;

public class SandboxTest {
    private static final Logger log = LoggerFactory.getLogger(SandboxTest.class);
    private final String contestFiles = Objects.requireNonNull(getClass().getClassLoader().getResource("testTask")).getPath();
    private final String commFiles = Objects.requireNonNull(getClass().getClassLoader().getResource("commTask")).getPath();
    private static final Task task = new Task("testTask",
            "1",
            "1",
            "correct.cpp",
            Language.CPP,
            1000,
            256 * 1024,
            "01",
            "01.in",
            "01.out",
            Task.CheckerType.TOKEN,
            TaskType.BATCH,
            1,
            Stage.COMPILATION);
    private static Sandbox sandbox;

    @BeforeAll
    public static void setUp() {
        log.error("Cleaning up environment...");
        DockerClient dockerClient = createDockerClient();
        dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()
                .stream()
                .filter(container -> container.getNames() != null &&
                        container.getNames().length > 0 &&
                        container.getNames()[0].contains("Worker-test1"))
                .forEach(container -> {
                    try {
                        dockerClient.removeContainerCmd(container.getId())
                                .withForce(true)
                                .exec();
                        log.error("Removed test container");
                    } catch (Exception e) {
                        log.error("Failed to remove container: " + e.getMessage());
                    }
                });
        sandbox = new Sandbox("test1");
        Config.setProperties("fileStorageDirectory.url", SandboxTest.class.getClassLoader().getResource("").getPath());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        log.error("Cleaning up environment...");
        if (sandbox != null) {
            sandbox.close();
        }
    }

    @Test
    public void testCompilation() {
        CompilationResult result = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("ce.cpp").getPath()));
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMessage().contains("was not declared in this scope"));
        result = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("correct.cpp").getPath()));
        assertTrue(result.isSuccess());
    }
    @Test
    public void testCorrect() throws Exception {
            CompilationResult compilationResult = sandbox.compile(task,
                    new File(getClass().getClassLoader().getResource("correct.cpp").getPath()));
            sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
            assertTrue(compilationResult.isSuccess());
            TestResult result = sandbox.execute(task);

            assertEquals(1.0, result.getScore());
            assertEquals(TestStatus.CORRECT, result.getStatus());
    }

    @Test
    public void testWA() throws Exception {
            CompilationResult compilationResult = sandbox.compile(task,
                    new File(getClass().getClassLoader().getResource("wa.cpp").getPath()));
            sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
            assertTrue(compilationResult.isSuccess());
            TestResult result = sandbox.execute(task);

            assertEquals(0.0, result.getScore());
            assertEquals(TestStatus.WRONG_ANSWER, result.getStatus());
    }

    @Test
    public void testML() throws Exception {
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("ml.cpp").getPath()));
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        assertTrue(compilationResult.isSuccess());
        TestResult result = sandbox.execute(task);

        assertEquals(0.0, result.getScore());
        assertEquals(TestStatus.MEMORY_LIMIT_EXCEEDED, result.getStatus());
    }

    @Test
    public void testCheater() throws Exception {
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("cheater.cpp").getPath()));
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        assertTrue(compilationResult.isSuccess());
        TestResult result = sandbox.execute(task);

        assertEquals(0.0, result.getScore(), result.getMessage());
    }

    @Test
    public void testYesNoChecker() throws Exception {
        Task task = new Task("testTask",
                "1",
                "1",
                "yesno.cpp",
                Language.CPP,
                1000,
                256 * 1024,
                "yesno",
                "yesno.in",
                "yesno.out",
                Task.CheckerType.YES_NO,
                TaskType.BATCH,
                1,
                Stage.TESTING);
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("yesno.cpp").getPath()));
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        assertTrue(compilationResult.isSuccess());
        TestResult result = sandbox.execute(task);

        assertEquals(1.0, result.getScore());
        assertEquals(TestStatus.CORRECT, result.getStatus());
    }

    @Test
    public void testYesNoWaChecker() throws Exception {
        Task task = new Task("testTask",
                "1",
                "1",
                "yesno.cpp",
                Language.CPP,
                1000,
                256 * 1024,
                "yesno",
                "yesno.in",
                "yesno.out",
                Task.CheckerType.YES_NO,
                TaskType.BATCH,
                1,
                Stage.TESTING);
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("yesno.cpp").getPath()));
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        assertTrue(compilationResult.isSuccess());
        TestResult result = sandbox.execute(task);

        assertEquals(1.0, result.getScore());
        assertEquals(TestStatus.CORRECT, result.getStatus());
    }

    @Test
    public void testDouble9Checker() throws Exception {
        Task task = new Task("testTask",
                "1",
                "1",
                "double9.cpp",
                Language.CPP,
                1000,
                256 * 1024,
                "double9",
                "double9.in",
                "double9.out",
                Task.CheckerType.DOUBLE_E9,
                TaskType.BATCH,
                1,
                Stage.TESTING);
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("double9.cpp").getPath()));
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        assertTrue(compilationResult.isSuccess());
        TestResult result = sandbox.execute(task);

        assertEquals(1.0, result.getScore());
        assertEquals(TestStatus.CORRECT, result.getStatus());
    }

    @Test
    public void testDouble9CheckerWA() throws Exception {
        Task task = new Task("testTask",
                "1",
                "1",
                "double9_2.cpp",
                Language.CPP,
                1000,
                256 * 1024,
                "double9",
                "double9.in",
                "double9.out",
                Task.CheckerType.DOUBLE_E9,
                TaskType.BATCH,
                1,
                Stage.TESTING);
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("double9_2.cpp").getPath()));
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        assertTrue(compilationResult.isSuccess());
        TestResult result = sandbox.execute(task);

        assertEquals(0.0, result.getScore());
        assertEquals(TestStatus.WRONG_ANSWER, result.getStatus());
    }

    /**
     * A submission must not be able to read the evaluator's code or the expected answer.
     * Anything it can read, it can print as its own output and exfiltrate.
     */
    @Test
    public void testSubmissionCanNotReadEvaluatorFiles() throws Exception {
        sandbox.uploadTar(compressFile(new File(commFiles), "commTask"), "/sandbox/tasks/");
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        CompilationResult compilation = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("attack_read.cpp").getPath()));
        assertTrue(compilation.isSuccess(), compilation.getErrorMessage());
        TestResult result = sandbox.execute(task);

        assertFalse(result.getOutcome().contains("LEAK"),
                "Submission read protected files:\n" + result.getOutcome());
    }

    /**
     * A submission must not be able to disarm the checker or forge a verdict.
     */
    @Test
    public void testSubmissionCanNotModifyEvaluatorFiles() throws Exception {
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        CompilationResult compilation = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("attack_write.cpp").getPath()));
        assertTrue(compilation.isSuccess(), compilation.getErrorMessage());
        TestResult result = sandbox.execute(task);

        assertFalse(result.getOutcome().contains("TAMPERED"),
                "Submission modified protected files:\n" + result.getOutcome());
        // The checker still has to work afterwards, and the forged answer must not be believed.
        assertEquals(TestStatus.WRONG_ANSWER, result.getStatus(), result.getMessage());
    }

    /**
     * The compiler quotes offending source lines and compilation errors are shown to the
     * contestant, so an #include of a secret file is an exfiltration channel.
     */
    @Test
    public void testSubmissionCanNotIncludeEvaluatorSource() throws Exception {
        sandbox.uploadTar(compressFile(new File(commFiles), "commTask"), "/sandbox/tasks/");
        CompilationResult compilation = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("attack_include.cpp").getPath()));

        assertFalse(compilation.isSuccess(), "Including a protected file should not compile");
        String message = compilation.getErrorMessage() == null ? "" : compilation.getErrorMessage();
        // The compiler must be refused outright. If it can open the file it quotes the
        // offending lines, and those diagnostics are shown to the contestant.
        assertTrue(message.contains("Permission denied") || message.contains("No such file"),
                "Expected the include to be refused, got:\n" + message);
        assertFalse(message.contains("secret_g2m") || message.contains("registerManager")
                        || message.contains("die_too_many_calls"),
                "Manager source leaked through compiler diagnostics:\n" + message);
    }

    /**
     * A file the teacher removes must disappear from the sandbox too. Uploads merge, so without
     * an explicit clear a grader moved to the manager slot keeps being linked into submissions
     * and fails them with a duplicate main.
     */
    @Test
    public void testTaskSyncRemovesDeletedFiles() throws Exception {
        sandbox.uploadTar(compressFile(new File(commFiles), "commTask"), "/sandbox/tasks/");
        // Simulate the misplaced manager still sitting in the graders directory.
        assertTrue(sandbox.fileExists("/sandbox/tasks/commTask/graders/grader.cpp"));

        sandbox.clearTaskDirectory("commTask");
        assertFalse(sandbox.fileExists("/sandbox/tasks/commTask/graders/grader.cpp"),
                "clearing must drop the cached copy before the next upload");

        sandbox.uploadTar(compressFile(new File(commFiles), "commTask"), "/sandbox/tasks/");
        assertTrue(sandbox.fileExists("/sandbox/tasks/commTask/graders/grader.cpp"),
                "re-upload must restore the current files");
    }

    /**
     * The sandbox must expose exactly one core, so a contestant cannot buy time with threads.
     * HostConfig.CpuCount, which this used to rely on, is a Windows-only field that the Linux
     * engine silently ignores - the cgroup cpuset is what actually enforces the limit.
     */
    @Test
    public void testSandboxIsPinnedToOneCore() throws Exception {
        String effective = sandbox.readFile("/sys/fs/cgroup/cpuset.cpus.effective");

        assertFalse(effective.contains("-") || effective.contains(","),
                "Sandbox should be pinned to a single core, cpuset is " + effective);
    }

    /**
     * A communication task fixture built from IOI 2026 "Ball Machine": the submission is linked
     * against the task's grader and judged by its manager over a FIFO pair.
     */
    private Task communicationTask(String submissionName, String testId) {
        return new Task("commTask",
                "1",
                "1",
                submissionName,
                Language.CPP,
                3500,
                256 * 1024,
                testId,
                testId + ".in",
                testId + ".out",
                Task.CheckerType.MANAGER,
                TaskType.COMMUNICATION,
                1,
                Stage.TESTING);
    }

    /**
     * Graders have to be in the sandbox before compilation, since they are linked into the
     * submission - unlike tests, which are only needed at run time.
     */
    private TestResult runCommunication(String solution, String testId) throws Exception {
        Task task = communicationTask(solution, testId);
        sandbox.uploadTar(compressFile(new File(commFiles), "commTask"), "/sandbox/tasks/");
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource(solution).getPath()));
        assertTrue(compilationResult.isSuccess(), compilationResult.getErrorMessage());
        return sandbox.execute(task);
    }

    @Test
    public void testCommunicationModelSolutionScoresFull() throws Exception {
        TestResult result = runCommunication("comm_model.cpp", "02");

        assertEquals(1.0, result.getScore(), result.getMessage());
        assertEquals(TestStatus.CORRECT, result.getStatus());
    }

    /**
     * The manager awards a fraction through testlib's quitp. This is the case the old
     * Long-typed callback score silently truncated to zero. Test 02 is large enough that this
     * solution's resource use costs it points; on small inputs it scores full marks.
     */
    @Test
    public void testCommunicationPartialSolutionScoresFraction() throws Exception {
        TestResult result = runCommunication("comm_partial.cpp", "02");

        assertEquals(TestStatus.PARTIAL, result.getStatus(), result.getMessage());
        assertEquals(0.55, result.getScore(), 1e-9, result.getMessage());
    }

    @Test
    public void testCommunicationWrongSolutionScoresZero() throws Exception {
        TestResult result = runCommunication("comm_empty.cpp", "02");

        assertEquals(0.0, result.getScore(), result.getMessage());
        assertEquals(TestStatus.WRONG_ANSWER, result.getStatus());
    }

    /**
     * The submission is charged its own CPU time, not the wall clock it shares with the manager.
     */
    @Test
    public void testCommunicationTimeExcludesManager() throws Exception {
        TestResult result = runCommunication("comm_model.cpp", "02");

        assertTrue(result.getTimeMillis() < 3500,
                "Expected the submission's own CPU time, got " + result.getTimeMillis() + "ms");
    }

    @Test
    public void testCorrectPython() throws Exception {
        Task task = new Task("testTask",
                "1",
                "1",
                "correct.py",
                Language.PYTHON,
                1000,
                256 * 1024,
                "01",
                "01.in",
                "01.out",
                Task.CheckerType.TOKEN,
                TaskType.BATCH,
                1,
                Stage.TESTING);
        CompilationResult compilationResult = sandbox.compile(task,
                new File(getClass().getClassLoader().getResource("correct.py").getPath()));
        sandbox.uploadTar(compressFile(new File(contestFiles), "testTask"), "/sandbox/tasks/");
        assertTrue(compilationResult.isSuccess());
        TestResult result = sandbox.execute(task);

        assertEquals(1.0, result.getScore());
        assertEquals(TestStatus.CORRECT, result.getStatus());
    }
}