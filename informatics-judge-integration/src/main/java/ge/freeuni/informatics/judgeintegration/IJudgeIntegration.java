package ge.freeuni.informatics.judgeintegration;

import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;

public interface IJudgeIntegration {

    void addSubmission(Task task, Submission submission) throws InformaticsServerException;

}
