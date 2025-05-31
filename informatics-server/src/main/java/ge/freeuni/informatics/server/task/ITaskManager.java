package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskInfo;

import java.io.File;
import java.util.*;

public interface ITaskManager {

    List<String> getTaskNames(long contestId, String language) throws InformaticsServerException;

    Task getTask(long taskId);

    TaskDTO addTask(TaskDTO task, long contestId) throws InformaticsServerException;

    List<TaskInfo> getUpsolvingTasks(long roomId, Integer offset, Integer limit) throws InformaticsServerException;


    Map<String, String> fillTaskNames(Long contestId);

    List<TaskInfo> getContestTasks(long contestId, int offset, int limit) throws InformaticsServerException;

    void removeTask(long taskId, long contest);

    File getStatement(long taskId, Language language) throws InformaticsServerException;

    void addStatement(long taskId, byte[] statement, Language language) throws InformaticsServerException;

    void addTestcase(long taskId, int testIndex, byte[] inputContent, byte[] outputContent) throws InformaticsServerException;

    void addTestcases(long taskId, byte[] testsZip) throws InformaticsServerException;

    void addManager(long taskId, byte[] manager);

    void removeManager(long taskId, String managerName);

    void removeTestCase(long taskId, long testcaseId);

}
