package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.dto.TaskDTO;

import java.util.List;

public class TaskInfo {

    private TaskDTO task;

    private Float score;

    private Float maxScore;

    private String contestName;

    /**
     * Empty - not null - whenever tags aren't shown for this viewer (a live contest the viewer
     * isn't staff on), so the frontend never has to distinguish "no tags" from "can't see them".
     */
    private List<String> tags = List.of();

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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? List.of() : tags;
    }
}
