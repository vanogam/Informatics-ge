package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.AddTestcasesResult;

public class AddTestcasesResponse extends InformaticsResponse {

    public AddTestcasesResponse(AddTestcasesResult result) {
        super(null);
        this.result = result;
    }

    public AddTestcasesResponse(String message) {
        super(null);
        this.result = result;
    }

    private AddTestcasesResult result;

    public AddTestcasesResult getResult() {
        return result;
    }

    public void setResult(AddTestcasesResult result) {
        this.result = result;
    }
}
