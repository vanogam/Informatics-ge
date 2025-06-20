package ge.freeuni.informatics.controller.model;

import org.springframework.web.multipart.MultipartFile;

public class AddSingleTestcaseRequest {

    MultipartFile inputFile;

    MultipartFile outputFile;

    Integer taskId;

    Integer testcaseId;

    public MultipartFile getInputFile() {
        return inputFile;
    }

    public void setInputFile(MultipartFile inputFile) {
        this.inputFile = inputFile;
    }

    public MultipartFile getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(MultipartFile outputFile) {
        this.outputFile = outputFile;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Integer getTestcaseId() {
        return testcaseId;
    }

    public void setTestcaseId(Integer testcaseId) {
        this.testcaseId = testcaseId;
    }
}
