package ge.freeuni.informatics.repository.contestroom;

import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class ContestRoomRepository implements IContestRoomRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public ContestRoom getRoom(Long roomId) {
        return em.find(ContestRoom.class, roomId);
    }

    @Override
    public void addRoom(ContestRoom room) {

    }

    @Override
    public List<ContestRoom> getRoomsForTeacher(Long teacherId) {
        return null;
    }
}
