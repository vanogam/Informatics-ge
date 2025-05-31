package ge.freeuni.informatics.repository.submission;

import ge.freeuni.informatics.common.model.submission.Submission;

import java.util.List;

public interface ISubmissionRepository {

    Submission addSubmission(Submission submission);

    List<Submission> getSubmissions(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit);

}
