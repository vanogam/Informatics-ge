package ge.freeuni.informatics.common.model.contest;

import ge.freeuni.informatics.common.model.task.Task;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

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

    private List<Long> participants;

    private List<Task> tasks;

    private boolean upsolvingAfterFinished;

    private boolean upsolving;

    private Standings standings;

    private Standings upsolvingStandings;

    private ScoringType scoringType;

    private Integer version;

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

    @ElementCollection
    public List<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public boolean isUpsolving() {
        return upsolving;
    }

    public void setUpsolving(boolean upsolving) {
        this.upsolving = upsolving;
    }

    public boolean isUpsolvingAfterFinished() {
        return upsolvingAfterFinished;
    }

    public void setUpsolvingAfterFinished(boolean upsolvingAfterFinished) {
        this.upsolvingAfterFinished = upsolvingAfterFinished;
    }

    @Type(type = StandingsType.TYPE)
    @Column(length = Integer.MAX_VALUE)
    public Standings getStandings() {
        return standings;
    }

    public void setStandings(Standings standings) {
        this.standings = standings;
    }

    @Type(type = StandingsType.TYPE)
    @Column(length = Integer.MAX_VALUE)
    public Standings getUpsolvingStandings() {
        return upsolvingStandings;
    }

    public void setUpsolvingStandings(Standings upsolvingStandings) {
        this.upsolvingStandings = upsolvingStandings;
    }

    public ScoringType getScoringType() {
        return scoringType;
    }

    public void setScoringType(ScoringType scoringType) {
        this.scoringType = scoringType;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
