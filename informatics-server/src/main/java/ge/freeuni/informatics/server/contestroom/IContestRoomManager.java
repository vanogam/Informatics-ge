package ge.freeuni.informatics.server.contestroom;

import ge.freeuni.informatics.common.model.contestroom.ContestRoom;

import java.util.List;

public interface IContestRoomManager {

    void createRoom(ContestRoom room);

    ContestRoom getRoom(Long roomId);

    void addTeachersToRoom(List<Long> teacherIds, Long roomId);

    void addParticipantsToRoom(List<Long> participantIds, Long roomId);

    List<ContestRoom> getRoomsForTeachers(Long teacherId);

    void getRoomsForParticipant(Long participantId);


}
