package ge.freeuni.informatics.model.entity.contestroom;

import ge.freeuni.informatics.model.entity.contest.Contest;
import ge.freeuni.informatics.model.entity.user.User;

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

    @ManyToMany
    public List<User> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<User> teachers) {
        this.teachers = teachers;
    }

    @ManyToMany
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
