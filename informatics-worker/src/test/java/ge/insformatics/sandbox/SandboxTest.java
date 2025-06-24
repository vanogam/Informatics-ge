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
}