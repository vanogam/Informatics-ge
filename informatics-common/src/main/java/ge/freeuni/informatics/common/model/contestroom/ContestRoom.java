package ge.freeuni.informatics.common.model.contestroom;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ContestRoom {

    @Transient
    public static final Integer GLOBAL_ROOM_ID = 1;

    private long id;

    private String name;

    private boolean open;

    private Set<Long> teachers;

    private Set<Long> participants;

    private Set<Long> contests;

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

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @ElementCollection
    public Set<Long> getTeachers() {
        return teachers;
    }

    public void setTeachers(Set<Long> teachers) {
        this.teachers = teachers;
    }

    @ElementCollection
    public Set<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Long> participants) {
        this.participants = participants;
    }

    @ElementCollection
    public Set<Long> getContests() {
        return contests;
    }

    public void setContests(Set<Long> contests) {
        this.contests = contests;
    }

    @Transient
    public boolean isMember(Long userId) {
        return participants.contains(userId) || teachers.contains(userId) || id == GLOBAL_ROOM_ID;
    }
}
