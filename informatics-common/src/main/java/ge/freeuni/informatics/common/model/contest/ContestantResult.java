package ge.freeuni.informatics.common.model.contest;

import jakarta.persistence.*;

import java.util.Map;

@Entity
@Table(name = "contestant_result", indexes = {
        @Index(name = "idx_total_score", columnList = "totalScore")
})
public class ContestantResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long contestantId;

    @ManyToOne
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne
    @JoinColumn(name = "upsolving_contest_id")
    private Contest upsolvingContest;

    @Column(name = "totalScore", nullable = false)
    private Float totalScore;
    @ElementCollection

    @CollectionTable(name = "task_results", joinColumns = @JoinColumn(name = "standings_id"))
    @MapKeyColumn(name = "task_code")
    @Column(name = "task_result")
    private Map<String, TaskResult> taskResults;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContestantId() {
        return contestantId;
    }

    public void setContestant(Long contestantId) {
        this.contestantId = contestantId;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public Contest getUpsolvingContest() {
        return upsolvingContest;
    }

    public void setUpsolvingContest(Contest upsolvingContest) {
        this.upsolvingContest = upsolvingContest;
    }

    public Float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Float totalScore) {
        this.totalScore = totalScore;
    }

    public Map<String, TaskResult> getTaskResults() {
        return taskResults;
    }

    public void setTaskResults(Map<String, TaskResult> taskResults) {
        this.taskResults = taskResults;
    }
}
