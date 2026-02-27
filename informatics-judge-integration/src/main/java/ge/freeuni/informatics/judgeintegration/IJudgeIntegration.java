package ge.freeuni.informatics.judgeintegration;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;

public interface IJudgeIntegration {

    void addSubmission(Task task, Submission submission) throws InformaticsServerException;

    void addCustomTest(Task task, CustomTestRun run, CodeLanguage language) throws InformaticsServerException;

}
