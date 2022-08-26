package ge.freeuni.informatics.controller.model;

import org.springframework.web.multipart.MultipartFile;

public class AddTestcasesRequest {

    MultipartFile file;

    Integer taskId;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }
}
