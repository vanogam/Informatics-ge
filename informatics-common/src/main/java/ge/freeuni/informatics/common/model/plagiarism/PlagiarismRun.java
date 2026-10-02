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
 * One JPlag invocation: every selected submission for one task, in one language. A
 * {@link PlagiarismJob} drives one of these per task/language pair it finds in scope.
 */
@Entity
@Table(name = "plagiarism_run")
public class PlagiarismRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /** The submissions' {@code language} value, e.g. "CPP" / "PYTHON". */
    @Column(name = "language", nullable = false, length = 20)
    private String language;

    @Column(name = "status", nullable = false)
    private PlagiarismRunStatus status = PlagiarismRunStatus.PENDING;

    @Column(name = "submission_count")
    private Integer submissionCount;

    @Column(name = "comparison_count")
    private Integer comparisonCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at")
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "finished_at")
    private Date finishedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public PlagiarismRunStatus getStatus() {
        return status;
    }

    public void setStatus(PlagiarismRunStatus status) {
        this.status = status;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

    public Integer getComparisonCount() {
        return comparisonCount;
    }

    public void setComparisonCount(Integer comparisonCount) {
        this.comparisonCount = comparisonCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlagiarismRun) {
            return Objects.equals(id, ((PlagiarismRun) obj).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
