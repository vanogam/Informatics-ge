package ge.freeuni.informatics.repository.submission;

import ge.freeuni.informatics.common.model.submission.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class SubmissionRepository implements ISubmissionRepository {

    final
    EntityManager em;

    @Autowired
    public SubmissionRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Submission addSubmission(Submission submission) {
        return em.merge(submission);
    }

    @Override
    public void registerSubmission(long submissionId, long judgeId) {
        Submission submission = em.find(Submission.class, submissionId);
        submission.setCmsId(judgeId);
    }

    @Override
    public Submission getSubmission(long id) {
        return em.find(Submission.class, id);
    }

    @Override
    public List<Submission> getSubmissions(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) {
        StringBuilder sql = new StringBuilder("SELECT s FROM Submission s WHERE 1 = 1 ");
        Map<String, Object> parameters = new HashMap<>();
        if (userId != null) {
            sql.append("AND s.userId = :userId ");
            parameters.put("userId", userId);
        }
        if (taskId != null) {
            sql.append("AND s.taskId = :taskId ");
            parameters.put("taskId", taskId);
        }
        if (contestId != null) {
            sql.append("AND s.contestId = :contestId ");
            parameters.put("contestId", contestId);
        }
        sql.append("AND s.roomId = :roomId ");
        parameters.put("roomId", roomId);
        TypedQuery<Submission> submissionQuery = em.createQuery(sql.toString(), Submission.class);
        for (String key : parameters.keySet()) {
            submissionQuery.setParameter(key, parameters.get(key));
        }
        if (offset != null) {
            submissionQuery.setFirstResult(offset);
        }
        if (limit != null) {
            submissionQuery.setMaxResults(limit);
        }
        return submissionQuery.getResultList();
    }

}
