package ge.freeuni.informatics.repository.contestroom;

import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ContestRoomJpaRepository extends JpaRepository<ContestRoom, Long> {

}
