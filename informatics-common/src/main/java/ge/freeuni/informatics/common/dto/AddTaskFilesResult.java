package ge.freeuni.informatics.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Outcome of a grader/manager upload: which files were stored and which were turned away.
 */
public class AddTaskFilesResult {

    private List<String> success = new ArrayList<>();

    private List<String> rejected = new ArrayList<>();

    public List<String> getSuccess() {
        return success;
    }

    public void setSuccess(List<String> success) {
        this.success = success;
    }

    public List<String> getRejected() {
        return rejected;
    }

    public void setRejected(List<String> rejected) {
        this.rejected = rejected;
    }
}