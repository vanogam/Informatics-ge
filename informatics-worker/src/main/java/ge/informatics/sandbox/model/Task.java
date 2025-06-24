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
                   Stage stage
) {

    public enum CheckerType {
        TOKEN("tokenChecker"),
        CUSTOM(null);

        private final String executable;

        CheckerType(String executable) {
            this.executable = executable;
        }

        public String getExecutable() {
            return executable;
        }
    }
}
