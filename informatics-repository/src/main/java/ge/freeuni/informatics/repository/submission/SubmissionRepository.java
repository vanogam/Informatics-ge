package ge.freeuni.informatics.repository.submission;

import ge.freeuni.informatics.model.entity.submission.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.List;

@Service
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
    public List<Submission> getSubmissions(long userId, long taskId) {
        String sql = "SELECT FROM Submission s WHERE";
        em.createQuery(sql).getResultList();
        return null;
    }

    @Override
    public List<Submission> getSubmissions(long userId) {
        return null;
    }
}
