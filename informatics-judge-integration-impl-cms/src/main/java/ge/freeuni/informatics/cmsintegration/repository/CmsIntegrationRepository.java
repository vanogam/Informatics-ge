package ge.freeuni.informatics.cmsintegration.repository;

import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Repository
@Transactional
public class CmsIntegrationRepository implements ICmsIntegrationRepository{

    @PersistenceContext
    EntityManager em;

    @Override
    public Task getTask(Integer taskId) {
        Task task = em.find(Task.class, taskId);
        em.detach(task);
        return task;
    }

    @Override
    public Task updateTask(Task task) {
        return em.merge(task);
    }

    @Override
    public Submission getSubmission(int submissionId) {
        Submission submission = em.find(Submission.class, submissionId);
        em.detach(submission);
        return submission;
    }

    @Override
    public Submission getSubmissionFromCmsId(int cmsId) {
        Submission submission = em.createQuery("SELECT s FROM Submission s WHERE s.cmsId = :cmsId", Submission.class)
                .setParameter("cmsId", (long) cmsId).getSingleResult();
        em.detach(submission);
        return submission;
    }

    @Override
    public Submission updateSubmission(Submission submission) {
        return em.merge(submission);
    }
}
