package ge.freeuni.informatics.repository.contest;

import ge.freeuni.informatics.common.model.contest.Contest;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class ContestRepository implements IContestRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void addContest(Contest contest) {
        em.persist(contest);
    }

    @Override
    public Contest getContest(Long contestId) {
        return null;
    }

    @Override
    public List<Contest> getRoomContests(Long roomId) {
        return null;
    }
}
