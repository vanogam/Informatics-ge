package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.AddTestcasesResult;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskInfo;

import java.io.File;
import java.util.*;

public interface ITaskManager {

    List<String> getTaskNames(long contestId, String language) throws InformaticsServerException;

    Task getTask(long taskId);

    TaskDTO addTask(long contestId, TaskDTO task) throws InformaticsServerException;

    List<TaskInfo> getUpsolvingTasks(long roomId, Integer offset, Integer limit) throws InformaticsServerException;

    Map<String, String> fillTaskNames(Long contestId);

    List<TaskInfo> getContestTasks(long contestId, int offset, int limit) throws InformaticsServerException;

    void removeTask(long taskId, long testId);

    String getStatement(long taskId, Language language) throws InformaticsServerException;

    void addStatement(long taskId, String statement, Language language);

    Task addTestcase(long taskId, byte[] inputContent, byte[] outputContent, String inputName, String outputName) throws InformaticsServerException;

    File getTestcaseZip(long taskId, String testcaseKey) throws InformaticsServerException;

    File getTestcasesZip(long taskId) throws InformaticsServerException;

    AddTestcasesResult addTestcases(long taskId, byte[] testsZip) throws InformaticsServerException;

    void addManager(long taskId, byte[] manager);

    void removeManager(long taskId, String managerName);

    void removeTestCase(long taskId, String testKey) throws InformaticsServerException;

}
