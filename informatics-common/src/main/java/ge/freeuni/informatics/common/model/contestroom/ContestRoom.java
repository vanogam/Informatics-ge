package ge.freeuni.informatics.common.model.contestroom;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.user.User;
import jakarta.persistence.*;

import java.util.Set;

@Entity(name = "contest_room")
public class ContestRoom {

    @Transient
    public static final long GLOBAL_ROOM_ID = 1;

    @Id
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean open;

    @OneToMany
    private Set<User> teachers;

    @OneToMany
    private Set<User> participants;

    @OneToMany
    private Set<Contest> contests;

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

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public Set<User> getTeachers() {
        return teachers;
    }

    public void setTeachers(Set<User> teachers) {
        this.teachers = teachers;
    }

    public Set<User> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<User> participants) {
        this.participants = participants;
    }

    public Set<Contest> getContests() {
        return contests;
    }

    public void setContests(Set<Contest> contests) {
        this.contests = contests;
    }

    @Transient
    public boolean isMember(Long userId) {
        return participants.contains(userId) || teachers.contains(userId) || id == GLOBAL_ROOM_ID;
    }
}
