package ge.freeuni.informatics.repository.contestroom;

import ge.freeuni.informatics.common.model.contestroom.ContestRoom;

import java.util.List;

public interface IContestRoomRepository {

    ContestRoom getRoom(Long roomId);

    void addRoom(ContestRoom room);

    List<ContestRoom> getRoomsForTeacher(Long teacherId);

}
