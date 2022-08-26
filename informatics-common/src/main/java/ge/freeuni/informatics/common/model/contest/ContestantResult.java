package ge.freeuni.informatics.common.model.contest;

import java.util.HashMap;

public class ContestantResult implements Comparable<ContestantResult> {

    private Float totalScore;

    private final long contestantId;

    private final HashMap<String, Float> scores;

    private final ScoringType type;

    public ContestantResult(ScoringType type, Integer contestantId) {
        this.type = type;
        this.scores = new HashMap<>();
        this.contestantId = contestantId;
    }

    public long getContestantId() {
        return contestantId;
    }

    public Float getTotalScore() {
        return totalScore;
    }

    public void removeTask(String taskCode) {
        if (scores.containsKey(taskCode)) {
            totalScore -= scores.get(taskCode);
        }
        scores.remove(taskCode);
    }

    public void setTaskScore(String taskCode, Float score) {
        Float initialScore = this.scores.get(taskCode);
        if (initialScore == null) {
            initialScore = 0F;
        }
        if (type == ScoringType.BEST_SUBMISSION) {
            this.scores.put(taskCode, Math.max(scores.get(taskCode) == null ? 0F : scores.get(taskCode), score));
        } else {
            this.scores.put(taskCode, score);
        }
        totalScore = totalScore + scores.get(taskCode) - initialScore;
    }

    public Float getTaskScore(String taskCode) {
        return scores.get(taskCode);
    }

    @Override
    public int compareTo(ContestantResult o) {
        return (int)(100F * totalScore - 100F * ((ContestantResult) o).totalScore);
    }
}
