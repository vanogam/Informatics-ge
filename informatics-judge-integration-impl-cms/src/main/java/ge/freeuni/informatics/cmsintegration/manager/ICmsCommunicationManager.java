package ge.freeuni.informatics.cmsintegration.manager;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;

public interface ICmsCommunicationManager {

    void addSubmission(Submission submission, Task task) throws InformaticsServerException;

    void addTask(Task task) throws InformaticsServerException;

    void addTestcases(Task task) throws InformaticsServerException;

}
