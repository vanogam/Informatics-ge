package ge.freeuni.informatics.judgeintegration;

import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.model.submission.Submission;

public interface IJudgeIntegration {

    void addTask(TaskDTO taskDTO);

    void addSubmission(String fileName, Submission submission);

    void editTask();


}
