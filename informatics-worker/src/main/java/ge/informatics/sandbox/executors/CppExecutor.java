package ge.informatics.sandbox.executors;

import com.github.dockerjava.api.DockerClient;
import ge.informatics.sandbox.ContainerPaths;
import ge.informatics.sandbox.Sandbox;
import ge.informatics.sandbox.Utils;
import ge.informatics.sandbox.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ge.informatics.sandbox.Utils.executeCommandSync;

public class CppExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(CppExecutor.class);

    @Override
    public String getSuffix() {
        return "cpp";
    }

    @Override
    public CompilationResult compileSubmission(DockerClient client, String containerId)
            throws IOException, InterruptedException {
        String sources = ContainerPaths.SUBMISSION_DIR + "/submission.cpp";
        String graderSources = graderSources(client, containerId);
        if (!graderSources.isEmpty()) {
            // A grader task provides main(); the submission only supplies the functions it calls.
            sources = sources + " " + graderSources;
            log.info("Linking submission against grader sources: {}", graderSources);
        }
        // Compiled as the contestant, never as root. The compiler quotes the source lines it
        // fails on, and those diagnostics go back to the contestant - so a root compile turns
        // #include "/sandbox/tasks/<id>/tests/01.out" into a way to read the expected answers.
        CompilationResult result = compileAs(client, containerId, Sandbox.CONTESTANT_USER, sources,
                ContainerPaths.submissionBinary(), "-I" + ContainerPaths.SUBMISSION_DIR);
        return new CompilationResult(result.isSuccess(), sanitizeDiagnostics(result.getErrorMessage()));
    }

    /**
     * Grader sources staged next to the submission, space separated, or an empty string when
     * the task supplies none.
     */
    private static String graderSources(DockerClient client, String containerId) throws InterruptedException {
        Utils.CommandResult listing = executeCommandSync(client, containerId,
                "ls " + ContainerPaths.SUBMISSION_DIR + "/*.cpp 2>/dev/null | grep -v '/submission\\.cpp$'");
        return listing.getStdout().toString(StandardCharsets.UTF_8).trim().replace("\n", " ");
    }

    /**
     * Strips sandbox paths out of compiler diagnostics. Contestants see this text, and it
     * would otherwise leak the judge's layout and the grader's file names.
     */
    static String sanitizeDiagnostics(String message) {
        if (message == null) {
            return null;
        }
        return message.replace(ContainerPaths.SUBMISSION_DIR + "/", "");
    }

    public static CompilationResult compile(DockerClient client, String containerId, String cppFile, String target)
            throws InterruptedException {
        return compile(client, containerId, cppFile, target, "");
    }

    /**
     * Compiles as root. Only for evaluator sources the task supplies - never for a submission.
     */
    public static CompilationResult compile(DockerClient client, String containerId, String cppFile, String target,
                                            String extraFlags) throws InterruptedException {
        return compileAs(client, containerId, null, cppFile, target, extraFlags);
    }

    /**
     * @param user unprivileged user to compile as, or null to compile as root
     */
    public static CompilationResult compileAs(DockerClient client, String containerId, String user, String cppFile,
                                              String target, String extraFlags) throws InterruptedException {
        log.info("Compiling C++ source(s) as {}: {}", user == null ? "root" : user, cppFile);
        String command = "g++ -std=c++20 -O2 " + (extraFlags == null ? "" : extraFlags)
                + " -o " + target + " " + cppFile;
        if (user != null) {
            command = "su -c \"" + command + "\" " + user;
        }
        Utils.CommandResult result = executeCommandSync(client, containerId, command);
        return new CompilationResult(result.getExitCode() == 0,
                result.getStderr().toString(StandardCharsets.UTF_8));
    }

    @Override
    public TestResult execute(DockerClient client, String containerId, Task task)
            throws InterruptedException, IOException {
        log.info("Executing C++ submission: {}", task.submissionId());
        long executionStart = System.currentTimeMillis();
        Utils.CommandResult result = executeCommandSync(
                client,
                containerId,
                "/usr/bin/time -v su -c '" + ContainerPaths.submissionBinary() + "' " + Sandbox.CONTESTANT_USER
                        + " < " + ContainerPaths.submissionInput() + " > " + ContainerPaths.submissionOutput(),
                task.timeLimitMillis() + 500,
                task.memoryLimitKB() + 10 * 1024
        );
        long runtime = System.currentTimeMillis() - executionStart;

        return buildTestResult(task, result, runtime, client, containerId);
    }

    @Override
    public String runCommand(Task task) {
        return ContainerPaths.submissionBinary();
    }
}
