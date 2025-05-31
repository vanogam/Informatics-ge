package ge.informatics.sandbox.model;


public class ExecutionResult {
    private final String submissionId;
    private final CompilationResult compilationResult;

    public ExecutionResult(String submissionId, CompilationResult compilationResult) {
        this.submissionId = submissionId;
        this.compilationResult = compilationResult;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public CompilationResult getCompilationResult() {
        return compilationResult;
    }
}
