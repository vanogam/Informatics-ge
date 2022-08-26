package ge.freeuni.informatics.repository.contest;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class ContestRepository implements IContestRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public Contest addContest(Contest contest) {
        return em.merge(contest);
    }

    @Override
    public Contest getContest(Long contestId) {
        return em.find(Contest.class, contestId);
    }

    @Override
    public List<Contest> getContests(Long roomId, String name, List<ContestStatus> statuses, Date minStartDate, Date maxStartDate) {
        StringBuilder sql = new StringBuilder("SELECT c FROM Contest c LEFT JOIN FETCH c.participants WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (roomId != null) {
            sql.append(" AND roomId = :roomId");
            params.put("roomId", roomId);
        }
        if (name != null) {
            sql.append(" AND name LIKE :name");
            params.put("name", "%" + name + "%");
        }
        if (statuses != null) {
            sql.append(" AND status in :statuses");
            params.put("statuses", statuses);
        }
        if (minStartDate != null) {
            sql.append(" AND startDate > :minStartDate");
            params.put("minStartDate", minStartDate);
        }
        if (maxStartDate != null) {
            sql.append(" AND startDate = :maxStartDate");
            params.put("maxStartDate", maxStartDate);
        }
        TypedQuery<Contest> query = em.createQuery(sql.toString(), Contest.class);
        for (String code : params.keySet()) {
            query.setParameter(code, params.get(code));
        }
        return query.getResultList();
    }

    @Override
    public void deleteContest(long contestId) {
        em.remove(em.find(Contest.class, contestId));
    }
}