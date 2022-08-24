package ge.freeuni.informatics.cmsintegration.repository;

import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;

public interface ICmsIntegrationRepository {

    Task getTask(Integer taskId);

    Task updateTask(Task task);

    Submission getSubmission(int submissionId);

    Submission getSubmissionFromCmsId(int cmsId);

    Submission updateSubmission(Submission submission);
}
