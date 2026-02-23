package ge.freeuni.informatics.common.model.contestroom;

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

    @ManyToMany
    @JoinTable(
            name = "contest_room_teachers",
            joinColumns = @JoinColumn(name = "contest_room_id"),
            inverseJoinColumns = @JoinColumn(name = "teachers_id")
    )
    private Set<User> teachers;

    @ManyToMany
    @JoinTable(
            name = "contest_room_participants",
            joinColumns = @JoinColumn(name = "contest_room_id"),
            inverseJoinColumns = @JoinColumn(name = "participants_id")
    )
    private Set<User> participants;

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

    @Transient
    public boolean isMember(Long userId) {
        return participants.stream().map(User::getId).anyMatch(userId::equals) ||
               teachers.stream().map(User::getId).anyMatch(userId::equals) || isOpen();
    }

    @Transient
    public boolean isTeacher(Long userId) {
        return teachers.stream().map(User::getId).anyMatch(userId::equals);
    }
}
