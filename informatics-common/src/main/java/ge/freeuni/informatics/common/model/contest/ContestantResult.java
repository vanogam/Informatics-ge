package ge.freeuni.informatics.common.model.contest;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

public class ContestantResult implements Comparable<ContestantResult> {

    private Float totalScore;

    private long contestantId;

    private final Map<String, Float> scores;

    private ScoringType type;

    public ContestantResult() {
        this.totalScore = 0F;
        this.scores = new HashMap<>();
    }

    public ContestantResult(ScoringType type, Integer contestantId) {
        this.type = type;
        this.totalScore = 0F;
        this.scores = new HashMap<>();
        this.contestantId = contestantId;
    }

    public long getContestantId() {
        return contestantId;
    }

    public Float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Float totalScore) {
        this.totalScore = totalScore;
    }

    public void setContestantId(long contestantId) {
        this.contestantId = contestantId;
    }

    @JsonAnyGetter
    public Map<String, Float> getScores() {
        return scores;
    }

    @JsonAnySetter
    public void set(String name, Float value) {
        scores.put(name, value);
    }

    public void setType(ScoringType type) {
        this.type = type;
    }

    public ScoringType getType() {
        return type;
    }

    public void removeTask(String taskCode) {
        if (scores.containsKey(taskCode)) {
            totalScore -= getTaskScore(taskCode);
        }
        scores.remove(taskCode);
    }

    public void setTaskScore(String taskCode, Float score) {
        if (!this.scores.containsKey(taskCode)) {
            this.scores.put(taskCode, 0F);
        }
        float initialScore = this.scores.get(taskCode);

        if (type == ScoringType.BEST_SUBMISSION) {
            float newScore = Math.max(initialScore, score);
            this.scores.put(taskCode, newScore);
        } else {
            this.scores.put(taskCode, score);
        }
        totalScore = totalScore + scores.get(taskCode) - initialScore;
    }

    public Float getTaskScore(String taskCode) {
        if (!this.scores.containsKey(taskCode)) {
            this.scores.put(taskCode, 0F);
        }
        return scores.get(taskCode);
    }

    @Override
    public int compareTo(ContestantResult o) {
        return (int)(100F * totalScore - 100F * o.totalScore);
    }
}
