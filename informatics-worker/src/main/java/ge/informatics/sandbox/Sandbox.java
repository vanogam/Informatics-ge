package ge.informatics.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.HostConfig;
import ge.informatics.sandbox.executors.CppExecutor;
import ge.informatics.sandbox.executors.Executor;
import ge.informatics.sandbox.fileservice.FileService;
import ge.informatics.sandbox.model.CompilationResult;

import ge.informatics.sandbox.model.Task;
import ge.informatics.sandbox.model.TestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static ge.informatics.sandbox.ContainerPaths.submissionBinary;
import static ge.informatics.sandbox.Utils.*;

public class Sandbox implements AutoCloseable {
    public static final String CONTESTANT_USER = "contestant";
    public static final String CHECKER_USER = "checker";
    private static final Logger log = LogManager.getLogger(Sandbox.class);
    private final FileService fileService;

    private final String id;
    private final DockerClient dockerClient;
    private String containerId;

    public Sandbox(String id) {
        this.id = id;
        this.dockerClient = createDockerClient();
        this.fileService = FileService.getInstance(Config.get("fileservice.type"));
        init();
    }

    private void init() {
        try {
            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withCpuCount(1L)
                    .withMemory(1024L * 1024 * 1024)
                    .withNetworkMode("none");

            CreateContainerResponse container = dockerClient.createContainerCmd("sandbox")
                    .withHostConfig(hostConfig)
                    .withName("Worker-" + id)
                    .withCmd("sh", "/launch/launch.sh")
                    .exec();

            containerId = container.getId();
            dockerClient.startContainerCmd(containerId).exec();
            waitForStartup();
            log.info("Docker container started with ID: {}", containerId);
            loadCheckers();
        } catch (Exception e) {
            log.error("Failed to start Docker container", e);
            try {
                close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }

    public void uploadTar(InputStream file, String destPath) {
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(file)
                .withRemotePath(destPath)
                .exec();
        log.info("File uploaded successfully");
    }

    public void downloadFile(String src, String dest) {
        dockerClient.copyArchiveFromContainerCmd(containerId, src)
                .withHostPath(dest)
                .exec();
        log.info("File copied successfully");
    }

    private void loadCheckers() throws IOException, InterruptedException {
        File checker = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("tokenChecker.cpp")).getFile());
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(compressFile(checker, "tokenChecker.cpp"))
                .withRemotePath("/sandbox/checkers/")
                .exec();
        CppExecutor.compile(dockerClient, containerId, "sandbox/checkers/tokenChecker.cpp", "/sandbox/checkers/tokenChecker");
        changePermissions(dockerClient, containerId, "sandbox/checkers/tokenChecker", CHECKER_USER, "700");
        executeCommandSync(dockerClient, containerId, "rm -rf /sandbox/checkers/tokenChecker.cpp");
    }

    private void waitForStartup() throws InterruptedException {
        int retries = 50;
        while (retries > 0) {
            log.info("Waiting for worker container to start ...");
            if (fileExists("/sandbox/submission")) {
                log.info("Worker container started successfully");
                break;
            } else {
                retries --;
                Thread.sleep(100);
            }
        }
        if (retries == 0) {
            log.error("Worker container failed to start");
            throw new RuntimeException("Worker container failed to start");
        }
    }

    /**
     * Compiles submission into binary and returns the result.
     *
     * @param task task and submission description
     * @param submission submission file.
     * @return Compilation result and message
     */
    public CompilationResult compile(Task task, File submission) {
        Executor executor = task.getLanguage().getExecutor();
        if (executor == null) {
            log.error("No executor found for language {}", task.getLanguage().getName());
            throw new RuntimeException("No executor found for language" + task.getLanguage().getName());
        }
        try {
            prepareEnvironment(submission, executor);
            log.info("Preparation done for submission: {}", task.getSubmissionId());
        } catch (Exception e) {
            log.error("Error while setting up environment for submission {}",task.getSubmissionId() ,e);
            throw new RuntimeException(e);
            // TODO: System error response
        }
        try {
            CompilationResult result = executor.compileSubmission(dockerClient, containerId);
            if (result.isSuccess()) {
                fileService.uploadFile(submissionBinary(), "submission" + task.getSubmissionId(), this);
            }
            log.info("Compilation result: submission {}, result {}", task.getSubmissionId(), result.isSuccess() ? "success" : "failed");
            return result;
        } catch (Exception e) {
            log.error("Error while compiling submission {}", task.getSubmissionId(), e);
            throw new RuntimeException(e);
            // TODO: System error response
        }
    }

    public TestResult execute(Task task) {
        try {
            Executor executor = task.getLanguage().getExecutor();
            loadChecker(task);
            loadSubmission(task);
            loadTest(task.getTestId(), task.getCode());
            log.debug("Test {} loaded for submission", task.getTestId());
            return executor.execute(dockerClient, containerId, task);
        } catch (Exception e) {
            log.error("Error while compiling submission {}",task.getSubmissionId(), e);
            throw new RuntimeException(e);
            // TODO: System error response
        }
    }

    private void loadChecker(Task task) throws InterruptedException, IOException {
        if (task.getCheckerType().getExecutable() == null) {
            copyFile("/sandbox/tasks/" + task.getCode() + "/checker", task.getCode(),
                    "/sandbox/checker/checker");
        } else {
            copyFile("/sandbox/checkers/" + task.getCheckerType().getExecutable(), "/sandbox/checker/checker");
            changePermissions(dockerClient, containerId, "/sandbox/checker/checker", CHECKER_USER, "700");
        }
    }

    private void prepareEnvironment(File submission, Executor executor) throws IOException, InterruptedException {
        clearSubmissionDirectory();

        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(compressFile(submission, "submission." + executor.getSuffix()))
                .withRemotePath("/sandbox/submission/")
                .exec();
    }

    private void clearSubmissionDirectory() throws InterruptedException {
        executeCommandSync(dockerClient, containerId, "rm -rf /sandbox/submission/*");
        log.debug("Cleared submission directory");
    }

    private void loadTest(String testId, String taskCode) throws IOException, InterruptedException {
        copyFile(String.format("/sandbox/tasks/%s/tests/%s.in", taskCode, testId), taskCode,
                "/sandbox/submission/input");
        copyFile(String.format("/sandbox/tasks/%s/tests/%s.out", taskCode, testId), taskCode,
                "/sandbox/checker/output");

        executeCommandSync(dockerClient, containerId, "touch /sandbox/submission/output");
        log.debug("Loaded test {} for task {}", testId, taskCode);
    }

    private void copyFile(String src, String remoteName, String dest) throws IOException, InterruptedException {
        if (!fileExists(src)) {
            fileService.downloadFile(remoteName, src, this);
        }
        copyFile(src, dest);
    }

    private void copyFile(String src, String dest) throws InterruptedException {
        CommandResult result = executeCommandSync(dockerClient, containerId, "cp " + src + " " + dest);
        if (result.getExitCode() != 0) {
            throw new RuntimeException("Error during copy: " + result.getStderr().toString(StandardCharsets.UTF_8));
        }
        log.debug("Copied file from {} to {}", src, dest);
    }


    private boolean fileExists(String path) throws InterruptedException {
        String out = executeCommandSync(dockerClient, containerId, "test -e " + path + " && echo exists")
                .getStdout()
                .toString(StandardCharsets.UTF_8);
        return out.trim().equals("exists");
    }

    @Override
    public void close() throws Exception {
        try {
            if (containerId != null) {
                dockerClient.stopContainerCmd(containerId).exec();
                dockerClient.removeContainerCmd(containerId).exec();
                log.info("Docker container destroyed");
            }
        } catch (Exception e) {
            log.error("Failed to destroy Docker container", e);
        } finally {
            dockerClient.close();
        }
    }
}