package ge.freeuni.informatics.common.model.contest;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TaskResult {

    private String taskCode;
    private Float score;
    /**
     * Under {@link ScoringType#SUBTASK_MAX}, the contestant's best-so-far award on each of the
     * task's subtasks, encoded by
     * {@link ge.freeuni.informatics.common.model.submission.SubtaskScores}; {@link #score} is its
     * sum. Null for every other scoring type, and whenever the vectors could not be merged.
     */
    @Column(name = "subtaskscores", length = 2000)
    private String subtaskScores;
    private Integer attempts;
    private Long successTime;

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getSubtaskScores() {
        return subtaskScores;
    }

    public void setSubtaskScores(String subtaskScores) {
        this.subtaskScores = subtaskScores;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Long getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Long successTime) {
        this.successTime = successTime;
    }
}