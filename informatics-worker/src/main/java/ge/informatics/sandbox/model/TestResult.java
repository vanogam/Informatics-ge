package ge.informatics.sandbox.model;


public class TestResult {
    private final double score;
    private final TestStatus status;
    private final Integer exitCode;
    private final long timeMillis;
    private final long memoryKB;
    private final String errorMessage;

    public TestResult(TestStatus status,
                      Integer exitCode,
                      String errorMessage,
                      long timeMillis,
                      long memoryKB,
                      double score) {
        this.status = status;
        this.exitCode = exitCode;
        this.errorMessage = errorMessage;
        this.timeMillis = timeMillis;
        this.memoryKB = memoryKB;
        this.score = score;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public static class Builder {
        private Double score;
        private TestStatus status;
        private Integer exitCode;
        private long timeMillis;
        private long memoryKB;
        private String errorMessage;

        public Builder(TestResult testResult) {
            this.score = testResult.getScore();
            this.status = testResult.status;
            this.exitCode = testResult.getExitCode();
            this.timeMillis = testResult.timeMillis;
            this.memoryKB = testResult.memoryKB;
            this.errorMessage = testResult.getErrorMessage();
        }
        public Builder() {
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

        public Builder withErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public TestResult build() {
            return new TestResult(status, exitCode, errorMessage, timeMillis, memoryKB, score);
        }
    }
}
