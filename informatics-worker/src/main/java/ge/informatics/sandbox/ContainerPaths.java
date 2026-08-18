package ge.informatics.sandbox;

/**
 * Absolute paths inside the sandbox container. The directories and their ownership are
 * created in src/main/docker/sandbox.Dockerfile.
 */
public class ContainerPaths {

    public static final String SUBMISSION_DIR = "/sandbox/submission";
    public static final String CHECKER_DIR = "/sandbox/checker";
    public static final String MANAGER_DIR = "/sandbox/manager";
    public static final String FIFO_DIR = "/sandbox/fifo";
    public static final String RUN_DIR = "/sandbox/run";
    public static final String TASKS_DIR = "/sandbox/tasks";
    public static final String BUILTIN_CHECKERS_DIR = "/sandbox/checkers";

    public static String submissionBinary() {
        return SUBMISSION_DIR + "/submission";
    }

    public static String submissionInput() {
        return SUBMISSION_DIR + "/input";
    }

    public static String submissionOutput() {
        return SUBMISSION_DIR + "/output";
    }

    public static String checkerBinary() {
        return CHECKER_DIR + "/checker";
    }

    public static String checkerAnswer() {
        return CHECKER_DIR + "/output";
    }

    public static String managerBinary() {
        return MANAGER_DIR + "/manager";
    }

    /**
     * Where the manager writes its score. Owned by the checker user so the contestant
     * cannot forge it.
     */
    public static String managerScore() {
        return CHECKER_DIR + "/manager_score";
    }

    public static String managerMessage() {
        return CHECKER_DIR + "/manager_message";
    }

    /**
     * Marker recording which version of the task the compiled manager was built from.
     */
    public static String managerStamp() {
        return MANAGER_DIR + "/built_from";
    }

    public static String taskDir(String taskId) {
        return TASKS_DIR + "/" + taskId;
    }

    public static String taskGradersDir(String taskId) {
        return taskDir(taskId) + "/graders";
    }

    public static String taskManagerDir(String taskId) {
        return taskDir(taskId) + "/manager";
    }

    public static String taskCheckerDir(String taskId) {
        return taskDir(taskId) + "/checker";
    }

    /**
     * A task's own checker, compiled and cached next to the built-in ones so it is copied into
     * place per test exactly the same way they are.
     */
    public static String customChecker(String taskId) {
        return BUILTIN_CHECKERS_DIR + "/task_" + taskId;
    }

    /**
     * Marker recording which version of the task the cached custom checker was built from.
     */
    public static String customCheckerStamp(String taskId) {
        return BUILTIN_CHECKERS_DIR + "/task_" + taskId + ".built_from";
    }

    public static String taskLastUpdate(String taskId) {
        return taskDir(taskId) + "/lastUpdate";
    }

    /**
     * FIFO carrying manager output to solution {@code index}; the solution reads it as stdin.
     */
    public static String fifoManagerToSolution(int index) {
        return FIFO_DIR + "/mgr_to_sol" + index;
    }

    /**
     * FIFO carrying solution {@code index} output to the manager; the solution writes it as stdout.
     */
    public static String fifoSolutionToManager(int index) {
        return FIFO_DIR + "/sol" + index + "_to_mgr";
    }

    /**
     * Where solution {@code index} leaves its stderr - its own diagnostics followed by the
     * /usr/bin/time report. One file per process, since they run at the same time and would
     * otherwise interleave into an unparseable stream.
     */
    public static String processStderr(int index) {
        return RUN_DIR + "/sol" + index + ".err";
    }

    /**
     * Where the exit status of solution {@code index} is recorded. The processes run detached,
     * so their statuses cannot be read from the exec's own exit code.
     */
    public static String processExitCode(int index) {
        return RUN_DIR + "/sol" + index + ".code";
    }
}