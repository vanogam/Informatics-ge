package ge.freeuni.informatics.common.model.contest;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Contest {

    private long id;

    private String name;

    private Date startDate;

    private Integer durationInSeconds;

    private ContestStatus status;

    private Long roomId;

    private List<User> participants;

    private List<Task> tasks;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public ContestStatus getStatus() {
        return status;
    }

    public void setStatus(ContestStatus status) {
        this.status = status;
    }

    @ManyToMany
    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    @OneToMany
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
