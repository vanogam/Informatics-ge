package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.AddTaskFilesResult;

public class AddTaskFilesResponse extends InformaticsResponse {

    private AddTaskFilesResult result;

    public AddTaskFilesResponse(AddTaskFilesResult result) {
        super(null);
        this.result = result;
    }

    public AddTaskFilesResponse(String message) {
        super(message);
    }

    public AddTaskFilesResult getResult() {
        return result;
    }

    public void setResult(AddTaskFilesResult result) {
        this.result = result;
    }
}