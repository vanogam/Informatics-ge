package ge.informatics.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import ge.informatics.sandbox.executors.CommunicationExecutor;
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
            String containerName = "Worker-" + id;
            handleExistingContainer(containerName);
            HostConfig hostConfig = HostConfig.newHostConfig()
                    // CpuCount is a Windows-only field that the Linux engine ignores. Pinning the
                    // container to a single core through the cgroup cpuset is what actually denies
                    // the contestant parallelism, and it cannot be undone with sched_setaffinity.
                    .withCpusetCpus(cpuSet())
                    .withMemory(containerMemoryBytes())
                    .withNetworkMode("none");

            CreateContainerResponse container = dockerClient.createContainerCmd(sandboxImage())
                    .withHostConfig(hostConfig)
                    .withName(containerName)
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

    /**
     * Image submissions are executed in. Configurable so a deployment can pull a versioned
     * image and keep a worker matched to the sandbox it was released with, instead of every
     * worker on the host racing for the same :latest tag.
     */
    private String sandboxImage() {
        String configured = Config.get("sandbox.image");
        return configured == null || configured.isBlank() ? "sandbox:latest" : configured.trim();
    }

    /**
     * The core this worker's sandbox is pinned to. Workers are numbered from 1 through APP_ID,
     * so each takes a distinct core and they do not contend with one another.
     */
    private String cpuSet() {
        String configured = Config.get("sandbox.cpuset");
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }
        int cores = Runtime.getRuntime().availableProcessors();
        int index;
        try {
            index = Math.abs(Integer.parseInt(id.replaceAll("\\D", ""))) % cores;
        } catch (NumberFormatException e) {
            index = Math.abs(id.hashCode()) % cores;
        }
        return String.valueOf(index);
    }

    /**
     * Ceiling for the whole container. It has to cover the submission's own limit plus the
     * manager running beside it; the submission is still held to its own limit by ulimit.
     */
    private long containerMemoryBytes() {
        String configured = Config.get("sandbox.memoryMB");
        long megabytes = 1024;
        if (configured != null && !configured.isBlank()) {
            try {
                megabytes = Long.parseLong(configured.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid sandbox.memoryMB value '{}', falling back to {} MB", configured, megabytes);
            }
        }
        return megabytes * 1024 * 1024;
    }

    private void handleExistingContainer(String containerName) {
        for (Container container : dockerClient.listContainersCmd()
                .withShowAll(true)
                .exec()) {
            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(container.getId()).exec();
            if (containerInfo.getName().equals("/" + containerName)) {
                dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
                log.info("Removed existing container with name: {}", containerName);
                return;
            }
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
        loadTestlibHeader();
        for (Task.CheckerType checkerType : Task.CheckerType.values()) {
            if (!checkerType.isTaskSupplied()) {
                loadChecker(checkerType.getExecutable());
                log.info("Checker {} loaded successfully", checkerType.getExecutable());
            } else {
                log.debug("Checker type {} is supplied by the task, nothing to preload", checkerType);
            }
        }
    }

    /**
     * Makes the bundled testlib header available to task-supplied managers and checkers, so a
     * task only has to upload its own source. A copy shipped with the task still wins, because
     * the task's own directory comes first on the include path.
     */
    private void loadTestlibHeader() throws IOException {
        File header = uploadResourceToTemp("testlib.h");
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(compressFile(header, "testlib.h"))
                .withRemotePath(ContainerPaths.BUILTIN_CHECKERS_DIR + "/")
                .exec();
        log.info("Bundled testlib header loaded into sandbox");
    }

    private File uploadResourceToTemp(String resourceName) throws IOException {
        InputStream inputStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(resourceName),
                "Missing bundled resource " + resourceName);
        File file = File.createTempFile("sandbox-resource", ".tmp");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(inputStream.readAllBytes());
        }
        return file;
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
            loadGraders(task);
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
            Executor executor = executorFor(task);
            // Each step below is a handful of docker execs at ~65ms apiece, so when a test feels
            // slow the breakdown says whether it is the submission or the scaffolding around it.
            long start = System.currentTimeMillis();
            clearSubmissionDirectory();
            long cleared = System.currentTimeMillis();
            loadChecker(task);
            if (task.isCommunication()) {
                prepareManager(task);
            }
            long evaluatorReady = System.currentTimeMillis();
            loadSubmission(task);
            long submissionLoaded = System.currentTimeMillis();
            loadTest(task);
            long testLoaded = System.currentTimeMillis();
            TestResult result = executor.execute(dockerClient, containerId, task);
            long finished = System.currentTimeMillis();

            log.info("Test {} timing (ms): clear={} evaluator={} submission={} test={} run={} total={}",
                    task.testId(), cleared - start, evaluatorReady - cleared,
                    submissionLoaded - evaluatorReady, testLoaded - submissionLoaded,
                    finished - testLoaded, finished - start);
            return result;
        } catch (Exception e) {
            log.error("Error while executing submission {}", task.submissionId(), e);
            throw new RuntimeException(e);
            // TODO: System error response
        }
    }

    /**
     * Communication tasks are run by the manager rather than by the language executor; the
     * language still decides how the binary is invoked.
     */
    private Executor executorFor(Task task) {
        Executor languageExecutor = task.language().getExecutor();
        return task.isCommunication() ? new CommunicationExecutor(languageExecutor) : languageExecutor;
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

    /**
     * Puts the checker binary in place for one test. A custom checker is treated exactly like a
     * built-in one: it is compiled once into /sandbox/checkers and copied in from there, so the
     * only difference between the two is where the source came from.
     */
    private void loadChecker(Task task) throws InterruptedException, IOException {
        if (task.isCommunication()) {
            // Judged by the manager, which runs alongside the submission; no checker involved.
            return;
        }
        String executable = task.checkerType().isTaskSupplied()
                ? prepareCustomChecker(task)
                : ContainerPaths.BUILTIN_CHECKERS_DIR + "/" + task.checkerType().getExecutable();

        copyFile(executable, ContainerPaths.checkerBinary());
        changePermissions(dockerClient, containerId, ContainerPaths.checkerBinary(), CHECKER_USER, "700");
    }

    /**
     * Compiles the task's own checker into /sandbox/checkers the first time it is needed, and
     * reuses it until the task changes.
     *
     * @return path of the compiled checker, ready to be copied in like a built-in one
     */
    private String prepareCustomChecker(Task task) throws InterruptedException, IOException {
        String executable = ContainerPaths.customChecker(task.taskId());
        String stamp = taskVersion(task.taskId());
        if (fileExists(executable) && stamp.equals(readFile(ContainerPaths.customCheckerStamp(task.taskId())))) {
            log.debug("Custom checker for task {} already built from version {}", task.taskId(), stamp);
            return executable;
        }
        buildTaskEvaluator(task, ContainerPaths.taskCheckerDir(task.taskId()), executable);
        executeCommandSync(dockerClient, containerId,
                "printf '%s' '" + stamp + "' > " + ContainerPaths.customCheckerStamp(task.taskId()));
        log.info("Built custom checker for task {} at version {}", task.taskId(), stamp);
        return executable;
    }

    /**
     * Copies the task's grader sources next to the submission so the compiler can link them in.
     * Does nothing for tasks that supply no graders.
     */
    private void loadGraders(Task task) throws InterruptedException {
        String gradersDir = ContainerPaths.taskGradersDir(task.taskId());
        if (!fileExists(gradersDir)) {
            return;
        }
        CommandResult result = executeCommandSync(dockerClient, containerId,
                "cp -r " + gradersDir + "/. " + ContainerPaths.SUBMISSION_DIR + "/");
        if (result.getExitCode() != 0) {
            throw new RuntimeException("Could not stage grader files: "
                    + result.getStderr().toString(StandardCharsets.UTF_8));
        }
        changePermissions(dockerClient, containerId, ContainerPaths.SUBMISSION_DIR + "/*",
                CONTESTANT_USER, "600");
        log.info("Staged grader files for task {}", task.taskId());
    }

    /**
     * Builds the task's manager once per task version and leaves it owned by the checker user.
     * Recompiling on every test would cost more than running it.
     */
    public void prepareManager(Task task) throws InterruptedException, IOException {
        String stamp = taskVersion(task.taskId());
        if (fileExists(ContainerPaths.managerBinary()) && stamp.equals(readFile(ContainerPaths.managerStamp()))) {
            log.debug("Manager for task {} already built from version {}", task.taskId(), stamp);
            return;
        }
        buildTaskEvaluator(task, ContainerPaths.taskManagerDir(task.taskId()), ContainerPaths.managerBinary());
        executeCommandSync(dockerClient, containerId,
                "printf '%s' '" + stamp + "' > " + ContainerPaths.managerStamp());
        log.info("Built manager for task {} at version {}", task.taskId(), stamp);
    }

    /**
     * Compiles the single C++ source the task supplies as its evaluator. The bundled testlib
     * header is on the include path, and a copy shipped with the task takes precedence.
     */
    private void buildTaskEvaluator(Task task, String sourceDir, String target)
            throws InterruptedException, IOException {
        if (!fileExists(sourceDir)) {
            throw new RuntimeException("Task " + task.taskId() + " supplies no source at " + sourceDir);
        }
        CommandResult sources = executeCommandSync(dockerClient, containerId,
                "ls " + sourceDir + "/*.cpp 2>/dev/null");
        String sourceList = sources.getStdout().toString(StandardCharsets.UTF_8).trim().replace("\n", " ");
        if (sourceList.isEmpty()) {
            throw new RuntimeException("Task " + task.taskId() + " supplies no evaluator source");
        }
        executeCommandSync(dockerClient, containerId,
                "mkdir -p " + target.substring(0, target.lastIndexOf('/')));
        CompilationResult result = CppExecutor.compile(dockerClient, containerId, sourceList, target,
                "-I" + sourceDir + " -I" + ContainerPaths.BUILTIN_CHECKERS_DIR);
        if (!result.isSuccess()) {
            log.error("Failed to compile evaluator for task {}: {}", task.taskId(), result.getErrorMessage());
            throw new RuntimeException("Failed to compile evaluator for task " + task.taskId() + ": "
                    + result.getErrorMessage());
        }
        changePermissions(dockerClient, containerId, target, CHECKER_USER, "700");
    }

    /**
     * The task's lastUpdate marker, or "0" when the task carries none.
     */
    private String taskVersion(String taskId) throws InterruptedException {
        String version = readFile(ContainerPaths.taskLastUpdate(taskId));
        return version == null || version.isEmpty() ? "0" : version.replaceAll("[^0-9]", "");
    }

    private void prepareEnvironment(File submission, Executor executor) throws IOException, InterruptedException {
        clearSubmissionDirectory();

        dockerClient.copyArchiveToContainerCmd(containerId)
                .withTarInputStream(compressFile(submission, "submission." + executor.getSuffix()))
                .withRemotePath("/sandbox/submission/")
                .exec();
        // Compilation runs as the contestant, so the sources must belong to them.
        changePermissions(dockerClient, containerId,
                ContainerPaths.SUBMISSION_DIR + "/submission." + executor.getSuffix(),
                CONTESTANT_USER, "600");
    }

    /**
     * Drops the sandbox's copy of a task before it is re-synced.
     *
     * <p>Uploading an archive merges into what is already there, so a file the teacher deleted
     * would otherwise survive here forever - a grader moved to the manager slot would still be
     * linked into every submission, and fail it with a duplicate main.
     */
    public void clearTaskDirectory(String taskId) throws InterruptedException {
        executeCommandSync(dockerClient, containerId, "rm -rf " + ContainerPaths.taskDir(taskId));
        log.debug("Cleared cached files for task {}", taskId);
    }

    void clearSubmissionDirectory() throws InterruptedException {
        executeCommandSync(dockerClient, containerId, "rm -rf /sandbox/submission/*");
        log.debug("Cleared submission directory");
    }

    private void loadTest(Task task) throws IOException, InterruptedException {
        String taskId = task.taskId();
        String baseDir = "/sandbox/tasks/" + taskId;
        String testsDirName = "tests";
        if ("custom".equals(task.testId())) {
            testsDirName = "custom-tests";
        }

        copyFile(String.format("%s/%s/%s", baseDir, testsDirName, task.inputName()), taskId,
                ContainerPaths.submissionInput());

        String answer = String.format("%s/%s/%s", baseDir, testsDirName, task.outputName());
        if (task.isCommunication() && !fileExists(answer)) {
            // The manager derives the expected result from the input, so many communication
            // tasks ship no answer files at all.
            executeCommandSync(dockerClient, containerId, "touch " + ContainerPaths.checkerAnswer());
        } else {
            copyFile(answer, taskId, ContainerPaths.checkerAnswer());
        }

        executeCommandSync(dockerClient, containerId, "touch " + ContainerPaths.submissionOutput());
        log.debug("Loaded test {}-{} for task {}", task.inputName(), task.outputName(), taskId);
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