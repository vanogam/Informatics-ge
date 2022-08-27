package ge.freeuni.informatics.cmsintegration.manager;

import ge.freeuni.informatics.cmsintegration.model.TestResult;
import ge.freeuni.informatics.cmsintegration.repository.ICmsIntegrationRepository;
import ge.freeuni.informatics.cmsintegration.utils.SubmissionResultHelper;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class CmsApiManager implements ICmsApiManager {

    @Autowired
    ICmsIntegrationRepository repository;

    @Autowired
    ApplicationEventPublisher publisher;

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
        submission.setStatus(SubmissionStatus.COMPILING);
        repository.updateSubmission(submission);
    }

    @Override
    public void setSubmissionCompilationResult(Integer cmsId, String result, String message) {
        Submission submission = repository.getSubmissionFromCmsId(cmsId);
        submission.setCompilationMessage(message.substring(0, Math.min(2000, message.length())));
        submission.setCompilationResult(result);
        if ("ok".equals(result)) {
            submission.setStatus(SubmissionStatus.RUNNING);
        } else {
            submission.setStatus(SubmissionStatus.FINISHED);
        }
        repository.updateSubmission(submission);
    }

    @Override
    public void setSubmissionTest(Integer cmsId, Integer testId) {
        Submission submission = repository.getSubmissionFromCmsId(cmsId);
        if (submission.getCurrentTest() == null) {
            submission.setCurrentTest(1);
        }
        submission.setCurrentTest(Math.max(submission.getCurrentTest(), testId + 1));
        repository.updateSubmission(submission);
    }

    @Override
    public void setSubmissionResult(Integer cmsId, Float score, TestResult[] result) {
        Submission submission = repository.getSubmissionFromCmsId(cmsId);
        submission.setScore(score);
        submission.setStatus(SubmissionStatus.FINISHED);
        submission.setSubmissionTestResultList(SubmissionResultHelper.toSubmissionTestResultList(result));
        repository.updateSubmission(submission);
        publisher.publishEvent(new SubmissionEvent(submission));
    }
    
}
