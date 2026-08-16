package ge.freeuni.informatics.common.model.task;

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
 * A source file uploaded for a task - a grader linked into submissions, or the manager
 * that judges them. The file on disk is what the worker reads; this row exists so the
 * editor can list, download and delete without scanning the file system.
 */
@Entity
@Table(name = "task_file")
public class TaskFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "kind", nullable = false)
    private TaskFileKind kind;

    @Column(name = "file_name", nullable = false, length = 100)
    private String fileName;

    @Column(name = "file_address", nullable = false, length = 1000)
    private String fileAddress;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "uploaded_at")
    private Date uploadedAt;

    /**
     * Whether contestants may download this file - true for headers and stubs they need
     * to compile against locally, false for the manager and anything else revealing.
     */
    @Column(name = "visible_to_contestants", nullable = false)
    private boolean visibleToContestants;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public TaskFileKind getKind() {
        return kind;
    }

    public void setKind(TaskFileKind kind) {
        this.kind = kind;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileAddress() {
        return fileAddress;
    }

    public void setFileAddress(String fileAddress) {
        this.fileAddress = fileAddress;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Date getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Date uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public boolean isVisibleToContestants() {
        return visibleToContestants;
    }

    public void setVisibleToContestants(boolean visibleToContestants) {
        this.visibleToContestants = visibleToContestants;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TaskFile) {
            return Objects.equals(id, ((TaskFile) obj).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}