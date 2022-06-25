package ge.freeuni.informatics.repository.submission;

import ge.freeuni.informatics.model.entity.submission.Submission;

import java.util.List;

public interface ISubmissionRepository {

    Submission addSubmission(Submission submission);

    void registerSubmission(long submissionId, long judgeId);

    Submission getSubmission(long id);

    List<Submission> getSubmissions(long userId, long taskId);

    List<Submission> getSubmissions(long userId);
}
