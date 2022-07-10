package ge.freeuni.informatics.common.model.task;

import javax.persistence.*;

@Entity
public class TestCase {

    private long id;

    private String inputFileAddress;

    private String OutputFileAddress;

//    private Task task;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

//    @ManyToOne(targetEntity = Task.class)
//    @JoinColumn(name = "taskId", nullable = false)
//    @Column(name = "taskId")
//    public Task getTask() {
//        return task;
//    }
//
//    public void setTask(Task task) {
//        this.task = task;
//    }
}
