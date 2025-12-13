package ge.freeuni.informatics.repository.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.model.contest.Contest;
import jakarta.persistence.TemporalType;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
@Transactional
public interface ContestJpaRepository extends JpaRepository<Contest, Long> {

    @Query("""
                SELECT c FROM Contest c WHERE
                    ( :roomId IS NULL OR c.roomId = :roomId) AND
                    ( :name IS NULL OR c.name LIKE %:name%) AND
                    ( CAST(:minStartDate AS timestamp) IS NULL OR (c.startDate IS NOT NULL AND c.startDate > CAST(:minStartDate AS timestamp))) AND
                    ( CAST(:minStartDate AS timestamp) IS NULL OR (c.startDate IS NOT NULL AND c.startDate <= CAST(:maxStartDate AS timestamp))) AND
                    ( CAST(:minStartDate AS timestamp) IS NULL OR (c.endDate IS NOT NULL AND c.endDate > CAST(:minEndDate AS timestamp))) AND
                    ( CAST(:minStartDate AS timestamp) IS NULL OR (c.endDate IS NOT NULL AND c.endDate <= CAST(:maxEndDate AS timestamp))) AND
                    ( :upsolving IS NULL OR c.upsolving = :upsolving)
            """)
    List<Contest> findContests(
            @Param("roomId") Long roomId,
            @Param("name") String name,
            @Param("minStartDate") @Temporal(TemporalType.TIMESTAMP) Date minStartDate,
            @Param("maxStartDate") @Temporal(TemporalType.TIMESTAMP) Date maxStartDate,
            @Param("minEndDate") @Temporal(TemporalType.TIMESTAMP) Date minEndDate,
            @Param("maxEndDate") @Temporal(TemporalType.TIMESTAMP) Date maxEndDate,
            @Param("upsolving") Boolean upsolving,
            Pageable pageable
    );

    default List<Contest> findContests(
            Long roomId,
            String name,
            Date minStartDate,
            Date maxStartDate,
            Date minEndDate,
            Date maxEndDate,
            Boolean upsolving,
            Pageable pageable,
            boolean loadParticipants,
            boolean loadTasks,
            boolean loadStandings,
            boolean loadUpsolvingStandings
    ) {
        List<Contest> contests = findContests(roomId, name, minStartDate, maxStartDate, minEndDate, maxEndDate, upsolving, pageable);
        return contests.stream().map(contest -> {
            if (loadParticipants) {
                Hibernate.initialize(contest.getParticipants());
            }
            if (loadTasks) {
                Hibernate.initialize(contest.getTasks());
            }
            if (loadStandings) {
                Hibernate.initialize(contest.getStandings());
            }
            if (loadUpsolvingStandings) {
                Hibernate.initialize(contest.getUpsolvingStandings());
            }
            return contest;
        }).toList();
    }

    @Query("""
                SELECT DISTINCT c FROM Contest c
                WHERE
                    c.roomId = :roomId AND
                    c.upsolving = true AND
                    (c.endDate IS NULL OR c.endDate <= :time)
            """)
    List<Contest> findUpsolvingContests(@Param("roomId") Long roomId,
                                        @Param("time") Date time);

    default Contest saveAndPublish(Contest contest, ApplicationEventPublisher eventPublisher) {
        contest = save(contest);
        eventPublisher.publishEvent(new ContestChangeEvent(ContestDTO.toDTO(contest)));
        return contest;
    }

    default Contest getById(Long id, boolean loadParticipants, boolean loadTasks, boolean loadStandings, boolean loadUpsolvingStandings) {
        Contest contest = getReferenceById(id);
        if (contest == null) {
            return null;
        }
        if (loadParticipants) {
            Hibernate.initialize(contest.getParticipants());
        }
        if (loadTasks) {
            Hibernate.initialize(contest.getTasks());
        }
        if (loadStandings) {
            Hibernate.initialize(contest.getStandings());
        }
        if (loadUpsolvingStandings) {
            Hibernate.initialize(contest.getUpsolvingStandings());
        }
        return contest;
    }
}
