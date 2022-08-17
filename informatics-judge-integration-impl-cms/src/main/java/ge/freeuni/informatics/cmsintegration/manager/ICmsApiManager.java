package ge.freeuni.informatics.cmsintegration.manager;

import ge.freeuni.informatics.cmsintegration.model.TestResult;

public interface ICmsApiManager {

    void registerTask(Integer cmsId, Integer taskId);

    void registerSubmission(Integer cmsId, Integer submissionId);

    void setSubmissionCompilationResult(Integer cmsId, String result, String message);

    void setSubmissionTest(Integer cmsId, Integer testId);

    void setSubmissionResult(Integer cmsId, Float score, TestResult[] result);
}
