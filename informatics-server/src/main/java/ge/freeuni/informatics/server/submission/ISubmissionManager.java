package ge.freeuni.informatics.server.submission;


import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.Submission;

public interface ISubmissionManager {

    void addSubmissionViaText(Submission submission, String text) throws InformaticsServerException;

    void registerSubmission(Long submissionId, Long cmsId);

}
