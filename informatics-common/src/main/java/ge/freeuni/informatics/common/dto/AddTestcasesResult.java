package ge.freeuni.informatics.common.dto;

import java.util.ArrayList;
import java.util.List;

public class AddTestcasesResult {

    private List<String> success = new ArrayList<>();

    private List<String> unmatched = new ArrayList<>();

    public List<String> getSuccess() {
        return success;
    }

    public void setSuccess(List<String> success) {
        this.success = success;
    }

    public List<String> getUnmatched() {
        return unmatched;
    }

    public void setUnmatched(List<String> unmatched) {
        this.unmatched = unmatched;
    }
}
