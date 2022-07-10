package ge.freeuni.informatics.judgeintegration;

import ge.freeuni.informatics.common.model.submission.Submission;

public interface IJudgeIntegration {

    void addTask();

    void addSubmission(String fileName, Submission submission);

    void editTask();


}
