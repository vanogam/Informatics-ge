package ge.informatics.sandbox.model;

public class Task {
    private final String code;
    private final Language language;
    private final String submissionId;
    private final long timeLimitMillis;
    private final int memoryLimitKB;
    private final String testId;
    private final CheckerType checkerType;

    public Task(String code, Language language, long timeLimitMillis, int memoryLimitKB, String testId, CheckerType checkerType, String submissionId) {
        this.code = code;
        this.language = language;
        this.timeLimitMillis = timeLimitMillis;
        this.memoryLimitKB = memoryLimitKB;
        this.testId = testId;
        this.checkerType = checkerType;
        this.submissionId = submissionId;
    }

    public String getCode() {
        return code;
    }

    public Language getLanguage() {
        return language;
    }

    public long getTimeLimitMillis() {
        return timeLimitMillis;
    }

    public int getMemoryLimitKB() {
        return memoryLimitKB;
    }

    public String getTestId() {
        return testId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public CheckerType getCheckerType() {
        return checkerType;
    }

    public enum CheckerType {
        TOKEN("tokenChecker"),
        CUSTOM(null);

        private String executable;

        CheckerType(String executable) {
            this.executable = executable;
        }

        public String getExecutable() {
            return executable;
        }
    }
}
