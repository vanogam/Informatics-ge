package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.AddTestcasesResult;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.dto.TaskMaterialsDTO;
import ge.freeuni.informatics.common.dto.TestcaseDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Statement;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskInfo;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ITaskManager {

    List<String> getTaskNames(long contestId, String language) throws InformaticsServerException;

    Task getTask(long taskId);

    TaskDTO addTask(long contestId, TaskDTO task) throws InformaticsServerException;

    /** @param tag exact (case-insensitive) match against one of the task's tags; null/blank matches every task. */
    List<TaskInfo> getUpsolvingTasks(long roomId, Integer offset, Integer limit, String tag) throws InformaticsServerException;

    /**
     * The total number of tasks {@link #getUpsolvingTasks} would page through for this room and
     * tag filter, ignoring offset/limit.
     */
    long getUpsolvingTasksCount(long roomId, String tag) throws InformaticsServerException;

    Map<String, String> fillTaskNames(Long contestId) throws InformaticsServerException;

    /** @param title case-insensitive substring match against the task title; null/blank matches every task. */
    List<TaskInfo> getContestTasks(long contestId, String title, int offset, int limit) throws InformaticsServerException;

    /**
     * The total number of tasks {@link #getContestTasks} would page through for this contest and
     * title filter, ignoring offset/limit.
     */
    long getContestTasksCount(long contestId, String title) throws InformaticsServerException;

    /**
     * Every task across every contest, for the admin submissions filter - the only task listing
     * not scoped to one contest or room. Reachable only through an {@code @AdminRestricted}
     * endpoint, since unlike {@link #getContestTasks} it carries no membership check of its own.
     *
     * @param title case-insensitive substring match against the task title; null/blank matches every task.
     */
    List<TaskInfo> getAllTasks(String title, int offset, int limit);

    /** The total count {@link #getAllTasks} would page through for this title filter, ignoring offset/limit. */
    long getAllTasksCount(String title);

    void removeTask(long taskId, long testId);

    Statement getStatement(long taskId, Language language) throws InformaticsServerException;

    List<TestcaseDTO> getPublicTestcases(long taskId) throws InformaticsServerException;

    void addStatement(long taskId, String statement, Language language) throws InformaticsServerException;

    /** Null when no editorial exists for that language, or it isn't visible to the current viewer. */
    String getEditorial(long taskId, Language language) throws InformaticsServerException;

    void addEditorial(long taskId, String editorial, Language language) throws InformaticsServerException;

    void setEditorialVisible(long taskId, boolean visible) throws InformaticsServerException;

    /** Only the languages that have non-blank code and are visible to the current viewer. */
    List<String> getSolutionLanguages(long taskId) throws InformaticsServerException;

    /** Null when no solution exists for that language, or it isn't visible to the current viewer. */
    String getSolution(long taskId, String language) throws InformaticsServerException;

    void addSolution(long taskId, String code, String language) throws InformaticsServerException;

    void removeSolution(long taskId, String language) throws InformaticsServerException;

    void setSolutionVisible(long taskId, boolean visible) throws InformaticsServerException;

    TaskMaterialsDTO getMaterialsAvailability(long taskId) throws InformaticsServerException;

    /** Empty when tags aren't visible to the current viewer (a live contest, if they're not staff). */
    List<String> getTags(long taskId) throws InformaticsServerException;

    void addTag(long taskId, String tagName) throws InformaticsServerException;

    void removeTag(long taskId, String tagName) throws InformaticsServerException;

    /** Every tag that exists, for a teacher's add-tag autocomplete. Not scoped to any one task. */
    List<String> getAllTagNames();

    AddTestcasesResult addTestcase(long taskId, byte[] inputContent, byte[] outputContent, String inputName, String outputName) throws InformaticsServerException;

    File getTestcaseZip(long taskId, String testcaseKey) throws InformaticsServerException;

    File getTestcasesZip(long taskId) throws InformaticsServerException;

    AddTestcasesResult addTestcases(long taskId, byte[] testsZip) throws InformaticsServerException;

    void setPublicTestcase(long taskId, String testcaseKey, boolean publicTestcase) throws InformaticsServerException;

    void removeTestCase(long taskId, String testKey) throws InformaticsServerException;

    void removeTestcases(long taskId, List<String> testKeys) throws InformaticsServerException;

    void updateTaskOrder(long contestId, List<Long> taskIds) throws InformaticsServerException;

}
