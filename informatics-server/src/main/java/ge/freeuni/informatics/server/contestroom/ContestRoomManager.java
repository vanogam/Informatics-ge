package ge.freeuni.informatics.server.contestroom;

import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContestRoomManager implements IContestRoomManager {

    @Autowired
    private ContestRoomJpaRepository contestRoomRepository;

//    public ContestRoomManager(ContestRoomJpaRepository contestRoomRepository) {
//        this.contestRoomRepository = contestRoomRepository;
//    }

    @Override
    @Secured({"ADMIN", "TEACHER"})
    public void createRoom(ContestRoom room) {
    }

    @Override
    public ContestRoom getRoom(Long roomId) {
        return contestRoomRepository.getReferenceById(roomId);
    }

    @Override
    public void addTeachersToRoom(List<Long> teacherIds, Long roomId) {

    }

    @Override
    public void addParticipantsToRoom(List<Long> participantIds, Long roomId) {

    }

    @Override
    @Secured({"ADMIN", "TEACHER"})
    public List<ContestRoom> getRoomsForTeachers(Long teacherId) {
        return null;
    }

    @Override
    public void getRoomsForParticipant(Long participantId) {

    }
}
