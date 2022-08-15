package ge.freeuni.informatics.cmsintegration.manager;

import ge.freeuni.informatics.cmsintegration.repository.ICmsIntegrationRepository;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CmsApiManager implements ICmsApiManager {

    @Autowired
    ICmsIntegrationRepository repository;

    @Override
    public void registerTask(Integer cmsId, Integer taskId) {
        Task task = repository.getTask(taskId);
        task.setJudgeId(cmsId);
        repository.updateTask(task);
    }

    @Override
    public void registerSubmission(Integer cmsId, Integer submissionId) {
        Submission submission = repository.getSubmission(submissionId);
        submission.setCmsId(cmsId);
        repository.updateSubmission(submission);
    }

    @Override
    public void setSubmissionCompilationResult(Integer cmsId, String result, String message) {
        Submission submission = repository.getSubmissionFromCmsId(cmsId);
        submission.setCompilationMessage(message);
        submission.setCompilationResult(result);
        repository.updateSubmission(submission);
    }
}
