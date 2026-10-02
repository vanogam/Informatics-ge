package ge.freeuni.informatics.common.model.plagiarism;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * One pair JPlag compared within a {@link PlagiarismRun}, with its similarity score. Usernames
 * are denormalized so the report list can be rendered without joining back to the submission
 * and user tables.
 */
@Entity
@Table(name = "plagiarism_comparison")
public class PlagiarismComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "submission_a_id", nullable = false)
    private Long submissionAId;

    @Column(name = "submission_b_id", nullable = false)
    private Long submissionBId;

    @Column(name = "username_a", nullable = false, length = 100)
    private String usernameA;

    @Column(name = "username_b", nullable = false, length = 100)
    private String usernameB;

    /** JPlag's overall similarity for the pair, in [0, 1]. */
    @Column(name = "similarity", nullable = false)
    private float similarity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public Long getSubmissionAId() {
        return submissionAId;
    }

    public void setSubmissionAId(Long submissionAId) {
        this.submissionAId = submissionAId;
    }

    public Long getSubmissionBId() {
        return submissionBId;
    }

    public void setSubmissionBId(Long submissionBId) {
        this.submissionBId = submissionBId;
    }

    public String getUsernameA() {
        return usernameA;
    }

    public void setUsernameA(String usernameA) {
        this.usernameA = usernameA;
    }

    public String getUsernameB() {
        return usernameB;
    }

    public void setUsernameB(String usernameB) {
        this.usernameB = usernameB;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlagiarismComparison) {
            return Objects.equals(id, ((PlagiarismComparison) obj).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
