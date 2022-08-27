package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;

import java.io.File;

public interface ITaskManager {

    Task getTask(int taskId);

    void addTask(TaskDTO task, long contestId) throws InformaticsServerException;

    void getUpsolvingTasks(long roomId, int offset, int limit);

    void removeTask(int taskId, long contest);

    File getStatement(int taskId, Language language) throws InformaticsServerException;

    void addStatement(int taskId, byte[] statement, Language language) throws InformaticsServerException;

    void addTestcase(int taskId, int testIndex, byte[] inputContent, byte[] outputContent) throws InformaticsServerException;

    void addTestcases(int taskId, byte[] testsZip) throws InformaticsServerException;

    void addManager(int taskId, byte[] manager);

    void removeManager(int taskId, String managerName);

    void removeTestCase(int taskId, long testcaseId);

}
