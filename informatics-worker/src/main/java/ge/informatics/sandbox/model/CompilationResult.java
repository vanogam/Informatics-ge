package ge.informatics.sandbox.model;

public class CompilationResult {

    private boolean success;
    private String errorMessage;

    public CompilationResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
