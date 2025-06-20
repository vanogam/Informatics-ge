package ge.freeuni.informatics.common.model.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class TestCase {

    private Long id;

    private String key;

    private Long taskId;

    private String inputFileAddress;

    private String OutputFileAddress;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getInputFileAddress() {
        return inputFileAddress;
    }

    public void setInputFileAddress(String inputFileAddress) {
        this.inputFileAddress = inputFileAddress;
    }

    public String getOutputFileAddress() {
        return OutputFileAddress;
    }

    public void setOutputFileAddress(String outputFileAddress) {
        OutputFileAddress = outputFileAddress;
    }

}
