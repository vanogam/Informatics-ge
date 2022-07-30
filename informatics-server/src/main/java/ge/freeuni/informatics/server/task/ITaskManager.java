package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;

import java.io.File;

public interface ITaskManager {

    void addTask(TaskDTO task, long contestId) throws InformaticsServerException;

    void removeTask(long taskId, long contest);

    void addStatement(long taskId, File statement, Language language);

    void addTestcases(long taskId, File testsZip);

    void addManager(long taskId, File manager);

    void removeManager(long taskId, String managerName);

    void removeTestCase(long taskId, long testcaseId);

    void updateTitle(String name, Language language);

}
