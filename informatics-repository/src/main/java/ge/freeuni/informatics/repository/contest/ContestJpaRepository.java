package ge.freeuni.informatics.repository.contest;

import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.utils.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface ContestJpaRepository extends JpaRepository<Contest, Long> {

    @Query("""
                SELECT c FROM Contest c WHERE
                    (:roomId IS NULL OR c.roomId = :roomId) AND
                    (:name IS NULL OR c.name LIKE %:name%) AND
                    (:minStartDate IS NULL OR c.startDate > :minStartDate) AND
                    (:maxStartDate IS NULL OR c.startDate <= :maxStartDate) AND
                    (:minEndDate IS NULL OR c.endDate > :minEndDate) AND
                    (:maxEndDate IS NULL OR c.endDate <= :maxEndDate) AND
                    (:upsolving IS NULL OR c.upsolving = :upsolving)
            """)
    List<Contest> findContests(
            @Param("roomId") Long roomId,
            @Param("name") String name,
            @Param("minStartDate") Date minStartDate,
            @Param("maxStartDate") Date maxStartDate,
            @Param("minStartDate") Date minEndDate,
            @Param("maxStartDate") Date maxEndDate,
            @Param("upsolving") Boolean upsolving,
            Pageable pageable
    );

    @Query("""
                SELECT c FROM Contest c WHERE
                    c.roomId = :roomId AND
                    c.upsolving = true AND
                    (c.endDate = NULL OR c.endDate <= :time)
            """)
    List<Contest> findUpsolvingContests(@Param("roomId") Long roomId,
                                        @Param("time") Date time);

    default Contest saveAndPublish(Contest contest) {
        contest = save(contest);
        BeanUtils.getBean(ApplicationEventPublisher.class).publishEvent(new ContestChangeEvent(contest));
        return contest;
    }

    default void removeParticipant(Long contestId, Long userId) {
        Contest contest = getReferenceById(contestId);

        if (contest.getParticipants() != null) {
            contest.getParticipants().removeIf(user -> user.getId() == userId);
        }

        saveAndPublish(contest);
    }

    default void addParticipant(Long contestId, User user) {
        Contest contest = getReferenceById(contestId);

        if (contest.getParticipants() == null) {
            contest.setParticipants(new ArrayList<>());
        }
        if (contest.getParticipants().stream().noneMatch(u -> u.getId() == user.getId())) {
            contest.getParticipants().add(user);
        }
        saveAndPublish(contest);
    }
}
