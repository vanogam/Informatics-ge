package ge.freeuni.informatics.controller.model;

import java.util.List;

/**
 * The result of an output upload: the submission it produced, and how much of the task it
 * actually covered - a contestant uploading a partial zip needs to see that before assuming the
 * missing tests were judged.
 */
public class OutputSubmitResponse extends SubmitResponse {

    private int matchedTests;
    private int totalTests;
    private List<String> unmatchedFiles;

    public OutputSubmitResponse() {
        super();
    }

    public OutputSubmitResponse(String message) {
        super(message);
    }

    public int getMatchedTests() {
        return matchedTests;
    }

    public void setMatchedTests(int matchedTests) {
        this.matchedTests = matchedTests;
    }

    public int getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }

    public List<String> getUnmatchedFiles() {
        return unmatchedFiles;
    }

    public void setUnmatchedFiles(List<String> unmatchedFiles) {
        this.unmatchedFiles = unmatchedFiles;
    }
}
