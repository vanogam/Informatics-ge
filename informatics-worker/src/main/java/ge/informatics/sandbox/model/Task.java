package ge.informatics.sandbox.model;

public record Task(String code, String contestId, Language language, long timeLimitMillis, int memoryLimitKB,
                   Stage stage, String testId, ge.informatics.sandbox.model.Task.CheckerType checkerType,
                   String submissionId) {

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
