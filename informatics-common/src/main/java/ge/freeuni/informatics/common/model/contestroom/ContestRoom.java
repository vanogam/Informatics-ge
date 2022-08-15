package ge.freeuni.informatics.common.model.contestroom;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.user.User;

import javax.persistence.*;
import java.util.List;

@Entity
public class ContestRoom {

    @Transient
    public static final String GLOBAL_ROOM_NAME = "GLOBAL ROOM";

    private long id;

    private String name;

    private List<User> teachers;

    private List<User> participants;


    private List<Contest> contests;

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

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "Room_Teacher",
            joinColumns = { @JoinColumn(name = "room_id") },
            inverseJoinColumns = { @JoinColumn(name = "teacher_id") }
    )
    public List<User> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<User> teachers) {
        this.teachers = teachers;
    }

    @ManyToMany
    @JoinTable(name = "Room_Participant",
            joinColumns = { @JoinColumn(name = "room_id") },
            inverseJoinColumns = { @JoinColumn(name = "participant_id") }
    )
    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    @OneToMany
    public List<Contest> getContests() {
        return contests;
    }

    public void setContests(List<Contest> contests) {
        this.contests = contests;
    }
}
