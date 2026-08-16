package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.dto.AddTaskFilesResult;
import ge.freeuni.informatics.common.dto.TaskFileDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.TaskFileKind;

import java.io.File;
import java.util.List;

/**
 * Manages the source files a task supplies to the judge: graders linked into submissions
 * and the manager that judges communication tasks.
 */
public interface ITaskFileManager {

    /**
     * Stores one source file, or every source file inside a ZIP.
     */
    AddTaskFilesResult addTaskFiles(long taskId, TaskFileKind kind, byte[] content, String fileName)
            throws InformaticsServerException;

    List<TaskFileDTO> getTaskFiles(long taskId) throws InformaticsServerException;

    /**
     * Files contestants are allowed to download - headers and stubs they compile against locally.
     */
    List<TaskFileDTO> getContestantVisibleFiles(long taskId) throws InformaticsServerException;

    File getTaskFile(long taskId, TaskFileKind kind, String fileName) throws InformaticsServerException;

    File getContestantVisibleFile(long taskId, String fileName) throws InformaticsServerException;

    void removeTaskFile(long taskId, TaskFileKind kind, String fileName) throws InformaticsServerException;

    void setFileVisibleToContestants(long taskId, TaskFileKind kind, String fileName, boolean visible)
            throws InformaticsServerException;
}