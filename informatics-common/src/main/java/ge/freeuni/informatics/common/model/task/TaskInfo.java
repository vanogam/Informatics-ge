package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.dto.TaskDTO;

public class TaskInfo {

    private TaskDTO task;

    private Float score;

    private Float maxScore;

    private String contestName;

    public TaskInfo(TaskDTO task, Float score) {
        this.task = task;
        this.score = score;
    }

    public TaskInfo(TaskDTO task, Float score, Float maxScore) {
        this.task = task;
        this.score = score;
        this.maxScore = maxScore;
    }

    public TaskInfo(TaskDTO task, Float score, Float maxScore, String contestName) {
        this.task = task;
        this.score = score;
        this.maxScore = maxScore;
        this.contestName = contestName;
    }

    public TaskInfo(TaskDTO task, Float score, String contestName) {
        this.task = task;
        this.score = score;
        this.contestName = contestName;
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

    public Float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Float maxScore) {
        this.maxScore = maxScore;
    }

    public String getContestName() {
        return contestName;
    }

    public void setContestName(String contestName) {
        this.contestName = contestName;
    }
}
