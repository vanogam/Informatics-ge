package ge.freeuni.informatics.repository.submission;

import ge.freeuni.informatics.common.model.submission.Submission;

import java.util.List;

public interface ISubmissionRepository {

    Submission addSubmission(Submission submission);

    void registerSubmission(long submissionId, long judgeId);

    Submission getSubmission(long id);

    List<Submission> getSubmissions(Long userId, Long taskId, Long roomId);

    List<Submission> getSubmissions(long userId);
}
