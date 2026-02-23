package ge.freeuni.informatics.common.model.contest;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private Date startDate;

    @Column
    private Date endDate;

    @Column
    private Long roomId;

    @ManyToMany
    @JoinTable(
            name = "Contest_principal",
            joinColumns = @JoinColumn(name = "Contest_id"),
            inverseJoinColumns = @JoinColumn(name = "participants_id")
    )
    private List<User> participants;

    @OneToMany
    private List<Task> tasks;

    @Column(nullable = false)
    private boolean upsolvingAfterFinished;

    @Column(nullable = false)
    private boolean upsolving;

    @OneToMany(mappedBy = "contest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContestantResult> standings;

    @OneToMany(mappedBy = "upsolvingContest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContestantResult> upsolvingStandings;

    @Column(nullable = false)
    private ScoringType scoringType;

    @Version
    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public boolean isUpsolvingAfterFinished() {
        return upsolvingAfterFinished;
    }

    public void setUpsolvingAfterFinished(boolean upsolvingAfterFinished) {
        this.upsolvingAfterFinished = upsolvingAfterFinished;
    }

    public boolean isUpsolving() {
        return upsolving;
    }

    public void setUpsolving(boolean upsolving) {
        this.upsolving = upsolving;
    }

    public List<ContestantResult> getStandings() {
        return standings;
    }

    public void setStandings(List<ContestantResult> standings) {
        this.standings = standings;
    }

    public List<ContestantResult> getUpsolvingStandings() {
        return upsolvingStandings;
    }

    public void setUpsolvingStandings(List<ContestantResult> upsolvingStandings) {
        this.upsolvingStandings = upsolvingStandings;
    }

    public ScoringType getScoringType() {
        return scoringType;
    }

    public void setScoringType(ScoringType scoringType) {
        this.scoringType = scoringType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Transient
    public ContestStatus getStatus() {
        Date now = new Date();
        if (startDate == null) {
            return ContestStatus.PAST;
        }
        if (startDate.after(now)) {
            return ContestStatus.FUTURE;
        } else if (endDate.before(now)) {
            return ContestStatus.PAST;
        } else {
            return ContestStatus.LIVE;
        }
    }

    @Transient
    public boolean hasParticipant(Long userId) {
        return participants != null && participants.stream().map(User::getId)
                .anyMatch(userId::equals);
    }
}
