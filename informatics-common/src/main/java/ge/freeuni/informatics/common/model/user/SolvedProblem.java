package ge.freeuni.informatics.common.model.user;

import ge.freeuni.informatics.common.model.task.Task;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "solved_problem", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "task_id"})
})
public class SolvedProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "solved_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date solvedAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProblemAttemptStatus status;

    @Column(name = "last_attempt_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAttemptAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Date getSolvedAt() {
        return solvedAt;
    }

    public void setSolvedAt(Date solvedAt) {
        this.solvedAt = solvedAt;
    }

    public ProblemAttemptStatus getStatus() {
        return status;
    }

    public void setStatus(ProblemAttemptStatus status) {
        this.status = status;
    }

    public Date getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Date lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SolvedProblem) {
            return ((SolvedProblem) obj).getId() != null && ((SolvedProblem) obj).getId().equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        return id.hashCode();
    }
}

