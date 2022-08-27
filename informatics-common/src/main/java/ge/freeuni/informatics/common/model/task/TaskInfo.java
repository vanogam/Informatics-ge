package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.dto.TaskDTO;

public class TaskInfo {

    private TaskDTO task;

    private Float score;

    public TaskInfo(TaskDTO task, Float score) {
        this.task = task;
        this.score = score;
    }

    public TaskDTO getTask() {
        return task;
    }

    public void setTask(TaskDTO task) {
        this.task = task;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
}
