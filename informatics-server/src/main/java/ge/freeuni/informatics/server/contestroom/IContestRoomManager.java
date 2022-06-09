package ge.freeuni.informatics.server.contestroom;

import ge.freeuni.informatics.model.entity.contestroom.ContestRoom;

import java.util.List;

public interface IContestRoomManager {

    void createRoom(ContestRoom room);

    void addTeachers(List<Long> teacherIds);

    void addParticipants(List<Long> participantIds);

    void getRoomsForTeachers(Long teacherId);

    void getRoomsForParticipant(Long participantId);


}
