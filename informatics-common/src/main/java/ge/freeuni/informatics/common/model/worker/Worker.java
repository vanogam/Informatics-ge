package ge.freeuni.informatics.common.model.worker;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "worker")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "worker_id", unique = true, nullable = false)
    private String workerId;

    @Column(name = "last_heartbeat", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastHeartbeat;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkerStatus status;

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "jobs_processed", nullable = false)
    private Long jobsProcessed;

    @Version
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Date getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(Date lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public WorkerStatus getStatus() {
        return status;
    }

    public void setStatus(WorkerStatus status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Long getJobsProcessed() {
        return jobsProcessed;
    }

    public void setJobsProcessed(Long jobsProcessed) {
        this.jobsProcessed = jobsProcessed;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}

