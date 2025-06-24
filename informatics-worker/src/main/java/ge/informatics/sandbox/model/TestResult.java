package ge.informatics.sandbox.model;


public class TestResult {
    private final CallbackType messageType;
    private final String testcaseKey;
    private final String message;
    private final long submissionId;
    private final Double score;
    private final TestStatus status;
    private final Integer exitCode;
    private final Long timeMillis;
    private final Long memoryKB;

    public TestResult(CallbackType messageType,
                      String testcaseKey,
                      String message,
                      long submissionId,
                      TestStatus status,
                      Integer exitCode,
                      Long timeMillis,
                      Long memoryKB,
                      Double score) {
        this.messageType = messageType;
        this.testcaseKey = testcaseKey;
        this.message = message;
        this.submissionId = submissionId;
        this.status = status;
        this.exitCode = exitCode;
        this.timeMillis = timeMillis;
        this.memoryKB = memoryKB;
        this.score = score;
    }

    public long getSubmissionId() {
        return submissionId;
    }

    public TestStatus getStatus() {
        return status;
    }

    public Double getScore() {
        return score;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public long getMemoryKB() {
        return memoryKB;
    }

    public CallbackType getMessageType() {
        return messageType;
    }

    public String getTestcaseKey() {
        return testcaseKey;
    }

    public String getMessage() {
        return message;
    }

    public static class Builder {
        private CallbackType messageType;
        private String testcaseKey;
        private String message;
        private long submissionId;
        private Double score;
        private TestStatus status;
        private Integer exitCode;
        private long timeMillis;
        private long memoryKB;

        public Builder(TestResult testResult) {
            this.messageType = testResult.messageType;
            this.testcaseKey = testResult.testcaseKey;
            this.message = testResult.message;
            this.submissionId = testResult.getSubmissionId();
            this.score = testResult.getScore();
            this.status = testResult.status;
            this.exitCode = testResult.getExitCode();
            this.timeMillis = testResult.timeMillis;
            this.memoryKB = testResult.memoryKB;
        }
        public Builder() {
        }

        public Builder withSubmissionId(long submissionId) {
            this.submissionId = submissionId;
            return this;
        }

        public Builder withMessageType(CallbackType messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder withTestcaseKey(String testcaseKey) {
            this.testcaseKey = testcaseKey;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withScore(Double score) {
            this.score = score;
            return this;
        }

        public Builder withStatus(TestStatus status) {
            this.status = status;
            return this;
        }

        public Builder withExitCode(Integer exitCode) {
            this.exitCode = exitCode;
            return this;
        }

        public Builder withTimeMillis(long timeMillis) {
            this.timeMillis = timeMillis;
            return this;
        }

        public Builder withMemoryKB(long memoryKB) {
            this.memoryKB = memoryKB;
            return this;
        }

        public TestResult build() {
            return new TestResult(messageType, testcaseKey, message, submissionId, status, exitCode, timeMillis, memoryKB, score);
        }
    }
}
