package ge.freeuni.informatics.common.model.plagiarism;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;
import java.util.Objects;

/**
 * One admin-triggered plagiarism check: either every task of a contest or a single task,
 * optionally narrowed to a set of users. Drives one {@link PlagiarismRun} per task/language pair
 * found in scope.
 */
@Entity
@Table(name = "plagiarism_job")
public class PlagiarismJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    /** Set XOR {@link #taskId} - the check's scope is either a whole contest or one task. */
    @Column(name = "contest_id")
    private Long contestId;

    @Column(name = "task_id")
    private Long taskId;

    /** Comma-joined usernames to narrow the check to; null means every user. */
    @Column(name = "usernames")
    private String usernames;

    @Column(name = "status", nullable = false)
    private PlagiarismJobStatus status = PlagiarismJobStatus.PENDING;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finished_at")
    private Date finishedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getUsernames() {
        return usernames;
    }

    public void setUsernames(String usernames) {
        this.usernames = usernames;
    }

    public PlagiarismJobStatus getStatus() {
        return status;
    }

    public void setStatus(PlagiarismJobStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlagiarismJob) {
            return Objects.equals(id, ((PlagiarismJob) obj).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
