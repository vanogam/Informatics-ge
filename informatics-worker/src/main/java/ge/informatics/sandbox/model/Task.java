package ge.informatics.sandbox.model;

public record Task(String taskId,
                   String contestId,
                   String submissionId,
                   String submissionName,
                   Language language,
                   long timeLimitMillis,
                   int memoryLimitKB,
                   String testId,
                   String inputName,
                   String outputName,
                   CheckerType checkerType,
                   TaskType taskType,
                   Integer numProcesses,
                   Stage stage,
                   /** Opaque token of the judging run; echoed back on every callback. */
                   Integer judgeToken,
                   /**
                    * Whether the contestant submitted code or their own output files. Null in
                    * messages published before the field existed, all of which are source.
                    */
                   SubmissionKind submissionKind
) {

    /**
     * True when the submission has to be judged by the task's manager, running alongside it.
     */
    public boolean isCommunication() {
        return taskType == TaskType.COMMUNICATION;
    }

    /**
     * Number of solution processes the manager drives. Defaults to 1 for messages published
     * before the field existed.
     */
    public int processCount() {
        return numProcesses == null || numProcesses < 1 ? 1 : numProcesses;
    }

    /**
     * True when the submission carries the answers themselves: nothing is compiled and nothing is
     * run, the uploaded file is simply handed to the checker.
     */
    public boolean isOutputSubmission() {
        return submissionKind == SubmissionKind.OUTPUT;
    }

    public enum SubmissionKind {
        SOURCE,
        OUTPUT
    }

    public enum CheckerType {
        TOKEN("tokenChecker"),
        YES_NO("yesNoChecker"),
        LINES("linesChecker"),
        DOUBLE_E6("double6Checker"),
        DOUBLE_E9("double9Checker"),
        CUSTOM(null),
        /**
         * The evaluator is the task's manager. Like CUSTOM it is supplied by the task rather
         * than built into the worker, so it has no bundled executable.
         */
        MANAGER(null);

        private final String executable;

        CheckerType(String executable) {
            this.executable = executable;
        }

        public String getExecutable() {
            return executable;
        }

        /**
         * True when the evaluator's source comes from the task directory and must be compiled
         * inside the sandbox before use.
         */
        public boolean isTaskSupplied() {
            return executable == null;
        }
    }
}