package ge.freeuni.informatics.cmsintegration.manager;

public interface ICmsApiManager {

    void registerTask(Integer cmsId, Integer taskId);

    void registerSubmission(Integer cmsId, Integer submissionId);

    void setSubmissionCompilationResult(Integer cmsId, String result, String message);

}
