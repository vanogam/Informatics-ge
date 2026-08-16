package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.TaskFileDTO;

import java.util.List;

public class TaskFilesResponse extends InformaticsResponse {

    private List<TaskFileDTO> files;

    public TaskFilesResponse(List<TaskFileDTO> files) {
        super(null);
        this.files = files;
    }

    public TaskFilesResponse(String message) {
        super(message);
    }

    public List<TaskFileDTO> getFiles() {
        return files;
    }

    public void setFiles(List<TaskFileDTO> files) {
        this.files = files;
    }
}