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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static ge.informatics.sandbox.ContainerPaths.submissionBinary;
import static ge.informatics.sandbox.Utils.*;

public class Sandbox implements AutoCloseable {
    public static final String CONTESTANT_USER = "contestant";
    public static final String CHECKER_USER = "checker";
    private static final Logger log = LoggerFactory.getLogger(Sandbox.class);
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
        log.info("File uploaded to container successfully");
    }

    public void downloadFile(String src, String dest) {
        try (InputStream inputStream = dockerClient.copyArchiveFromContainerCmd(containerId, src)
                .exec()) {
            FileOutputStream fos = new FileOutputStream(dest);
            fos.write(inputStream.readAllBytes());
        } catch (IOException e) {
            log.error("Error while writing file to destination: {}", dest, e);
            throw new RuntimeException("Error while writing file to destination: " + dest, e);
        }
        log.info("File copied successfully");
    }

    private void loadCheckers() throws IOException, InterruptedException {
        for (Task.CheckerType checkerType : Task.CheckerType.values()) {
            if (checkerType.getExecutable() != null) {
                loadChecker(checkerType.getExecutable());
                log.info("Checker {} loaded successfully", checkerType.getExecutable());
            } else {
                log.warn("No executable found for checker type {}", checkerType);
            }
        }
    }
    private void loadChecker(String name) throws IOException, InterruptedException {
        InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(name + ".cpp"));
        File checker = File.createTempFile("checker", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(checker)) {
            fos.write(inputStream.readAllBytes());
        } catch (IOException e) {
            log.error("Error while writing "+ name + ".cpp to /tmp", e);
            throw new RuntimeException("Error while writing "+ name + ".cpp to /tmp", e);
        }
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(compressFile(checker, name + ".cpp"))
                .withRemotePath("/sandbox/checkers/")
                .exec();
        CompilationResult result = CppExecutor.compile(dockerClient, containerId, "/sandbox/checkers/" + name + ".cpp", "/sandbox/checkers/" + name);
        if (!result.isSuccess()) {
            log.error("Failed to compile checker: {}", result.getErrorMessage());
            throw new RuntimeException("Failed to compile checker: " + result.getErrorMessage());
        }
        changePermissions(dockerClient, containerId, "/sandbox/checkers/" + name, CHECKER_USER, "700");
        executeCommandSync(dockerClient, containerId, "rm -rf /sandbox/checkers/" + name + ".cpp");
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
        Executor executor = task.language().getExecutor();
        if (executor == null) {
            log.error("No executor found for language {}", task.language().getName());
            throw new RuntimeException("No executor found for language" + task.language().getName());
        }
        try {
            prepareEnvironment(submission, executor);
            log.info("Preparation done for submission: {}", task.submissionId());
        } catch (Exception e) {
            log.error("Error while setting up environment for submission {}",task.submissionId() ,e);
            throw new RuntimeException(e);
        }
        try {
            CompilationResult result = executor.compileSubmission(dockerClient, containerId);
            if (result.isSuccess()) {
                fileService.uploadFile(submissionBinary(), "submission" + task.submissionId(), this);
            }
            log.info("Compilation result: submission {}, result {}", task.submissionId(), result.isSuccess() ? "success" : "failed");
            return result;
        } catch (Exception e) {
            log.error("Error while compiling submission {}", task.submissionId(), e);
            throw new RuntimeException(e);
        }
    }

    public TestResult execute(Task task) {
        try {
            Executor executor = task.language().getExecutor();
            clearSubmissionDirectory();
            loadChecker(task);
            loadSubmission(task);
            loadTest(task.taskId(), task.inputName(), task.outputName());
            log.debug("Test {} loaded for submission", task.testId());
            return executor.execute(dockerClient, containerId, task);
        } catch (Exception e) {
            log.error("Error while compiling submission {}",task.submissionId(), e);
            throw new RuntimeException(e);
            // TODO: System error response
        }
    }

    public String retrieveOutcome() throws InterruptedException {
        String outputPath = "/sandbox/submission/output";
        return executeCommandSync(dockerClient, containerId, "head -c 1000 " + outputPath)
                .getStdout().toString(StandardCharsets.UTF_8);
    }

    private void loadSubmission(Task task) throws IOException, InterruptedException {
        String remotePath = Config.get("sharedDirectory.url") + "/submission" + task.submissionId();
        fileService.downloadFile(remotePath, "/sandbox/submission", "submission", this, false);
        changePermissions(dockerClient, containerId,
                "/sandbox/submission/submission",
                CONTESTANT_USER, "700");
        log.info("Submission loaded for task {}", task.taskId());
    }

    private void loadChecker(Task task) throws InterruptedException, IOException {
        if (task.checkerType().getExecutable() == null) {
            copyFile("/sandbox/tasks/" + task.taskId() + "/checker", task.taskId(),
                    "/sandbox/checker/checker");
        } else {
            copyFile("/sandbox/checkers/" + task.checkerType().getExecutable(), "/sandbox/checker/checker");
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

    void clearSubmissionDirectory() throws InterruptedException {
        executeCommandSync(dockerClient, containerId, "rm -rf /sandbox/submission/*");
        log.debug("Cleared submission directory");
    }

    private void loadTest(String taskId, String inputName, String outputName) throws IOException, InterruptedException {
        copyFile(String.format("/sandbox/tasks/%s/tests/%s", taskId, inputName), taskId,
                "/sandbox/submission/input");
        copyFile(String.format("/sandbox/tasks/%s/tests/%s", taskId, outputName), taskId,
                "/sandbox/checker/output");

        executeCommandSync(dockerClient, containerId, "touch /sandbox/submission/output");
        log.debug("Loaded test {}-{} for task {}", inputName, outputName, taskId);
    }

    private void copyFile(String src, String remoteName, String dest) throws InterruptedException, IOException {
        if (!fileExists(src)) {
            String secDir = src.substring(0, src.lastIndexOf("/"));
            String srcName = src.substring(src.lastIndexOf("/") + 1);
            fileService.downloadFile(Config.get("fileStorageDirectory.url") + "/" + remoteName, secDir, srcName, this, true);
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


    public boolean fileExists(String path) throws InterruptedException {
        String out = executeCommandSync(dockerClient, containerId, "test -e " + path + " && echo exists")
                .getStdout()
                .toString(StandardCharsets.UTF_8);
        return out.trim().equals("exists");
    }

    public String readFile(String path) throws InterruptedException {
        String out = executeCommandSync(dockerClient, containerId, "cat " + path)
                .getStdout()
                .toString(StandardCharsets.UTF_8);
        return out.trim();
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