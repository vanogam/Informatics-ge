package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.dto.AddTaskFilesResult;
import ge.freeuni.informatics.common.dto.TaskFileDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.TaskFile;
import ge.freeuni.informatics.common.model.task.TaskFileKind;
import ge.freeuni.informatics.repository.task.TaskFileRepository;
import ge.freeuni.informatics.server.annotation.MemberTaskRestricted;
import ge.freeuni.informatics.server.annotation.TeacherTaskRestricted;
import ge.freeuni.informatics.utils.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class TaskFileManager implements ITaskFileManager {

    /**
     * Source files only. Anything the judge would have to execute as-is is refused, so a
     * teacher cannot smuggle a prebuilt binary onto a worker.
     */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "cpp", "cc", "cxx", "c", "h", "hpp", "hxx", "py", "java", "txt", "md"
    );

    private static final int MAX_FILE_NAME_LENGTH = 100;

    private static final long MAX_UNPACKED_BYTES = 32L * 1024 * 1024;

    @Autowired
    Logger log;

    @Autowired
    TaskFileRepository taskFileRepository;

    @Value("${ge.freeuni.informatics.Task.taskDirectoryAddress}")
    String taskDirectoryAddress;

    @Override
    @Transactional
    @TeacherTaskRestricted
    public AddTaskFilesResult addTaskFiles(long taskId, TaskFileKind kind, byte[] content, String fileName)
            throws InformaticsServerException {
        AddTaskFilesResult result = new AddTaskFilesResult();
        Map<String, byte[]> files = isZip(fileName) ? unpackZip(content, result) : singleFile(fileName, content, result);

        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            rejectMisplacedEvaluator(kind, entry.getKey(), entry.getValue());
            store(taskId, kind, entry.getKey(), entry.getValue());
            result.getSuccess().add(entry.getKey());
        }
        if (!result.getSuccess().isEmpty()) {
            touchLastUpdate(taskId);
        }
        return result;
    }

    @Override
    @TeacherTaskRestricted
    public List<TaskFileDTO> getTaskFiles(long taskId) throws InformaticsServerException {
        return TaskFileDTO.toDTOs(taskFileRepository.findByTaskIdOrderByKindAscFileNameAsc(taskId));
    }

    @Override
    @MemberTaskRestricted
    public List<TaskFileDTO> getContestantVisibleFiles(long taskId) throws InformaticsServerException {
        return TaskFileDTO.toDTOs(
                taskFileRepository.findByTaskIdAndVisibleToContestantsOrderByFileNameAsc(taskId, true));
    }

    @Override
    @TeacherTaskRestricted
    public File getTaskFile(long taskId, TaskFileKind kind, String fileName) throws InformaticsServerException {
        TaskFile taskFile = requireFile(taskId, kind, sanitizeFileName(fileName));
        return resolveExisting(taskFile);
    }

    @Override
    @MemberTaskRestricted
    public File getContestantVisibleFile(long taskId, String fileName) throws InformaticsServerException {
        String safeName = sanitizeFileName(fileName);
        TaskFile taskFile = taskFileRepository
                .findByTaskIdAndVisibleToContestantsOrderByFileNameAsc(taskId, true)
                .stream()
                .filter(f -> f.getFileName().equals(safeName))
                .findFirst()
                .orElseThrow(() -> new InformaticsServerException("taskFileNotFound"));
        return resolveExisting(taskFile);
    }

    @Override
    @Transactional
    @TeacherTaskRestricted
    public void removeTaskFile(long taskId, TaskFileKind kind, String fileName) throws InformaticsServerException {
        TaskFile taskFile = requireFile(taskId, kind, sanitizeFileName(fileName));
        try {
            Files.deleteIfExists(Paths.get(taskFile.getFileAddress()));
        } catch (IOException e) {
            log.error("Error while deleting task file {} of task {}", fileName, taskId, e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
        taskFileRepository.delete(taskFile);
        touchLastUpdate(taskId);
    }

    @Override
    @Transactional
    @TeacherTaskRestricted
    public void setFileVisibleToContestants(long taskId, TaskFileKind kind, String fileName, boolean visible)
            throws InformaticsServerException {
        if (visible && kind != TaskFileKind.GRADER) {
            // Managers and checkers hold the reference solution and the scoring rules.
            throw new InformaticsServerException("evaluatorCanNotBePublished");
        }
        TaskFile taskFile = requireFile(taskId, kind, sanitizeFileName(fileName));
        taskFile.setVisibleToContestants(visible);
        taskFileRepository.save(taskFile);
    }


    /**
     * Refuses a manager or checker uploaded into the graders slot.
     *
     * <p>Graders are linked into every submission, so a misplaced evaluator breaks compilation
     * for everyone: first "testlib.h: No such file" (the submission compile has no testlib on
     * its include path), then "multiple definition of main" once that is worked around. Both
     * errors name the student's compile, which points nowhere near the actual mistake.
     *
     * <p>An evaluator is unmistakable - it uses testlib, which a grader never does.
     */
    private void rejectMisplacedEvaluator(TaskFileKind kind, String fileName, byte[] content)
            throws InformaticsServerException {
        if (kind != TaskFileKind.GRADER || !isSource(fileName)) {
            return;
        }
        String text = new String(content, StandardCharsets.UTF_8);
        if (text.contains("testlib.h") || text.contains("registerManager")
                || text.contains("registerChecker")) {
            log.warn("Refused evaluator source '{}' uploaded as a grader", fileName);
            throw new InformaticsServerException("evaluatorUploadedAsGrader");
        }
    }

    private boolean isSource(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".cpp") || lower.endsWith(".cc") || lower.endsWith(".cxx")
                || lower.endsWith(".c");
    }

    private void store(long taskId, TaskFileKind kind, String safeName, byte[] content)
            throws InformaticsServerException {
        String directory = directoryFor(taskId, kind);
        String address = FileUtils.buildPath(directory, safeName);
        try {
            Files.createDirectories(Paths.get(directory));
            Files.write(Paths.get(address), content);
        } catch (IOException e) {
            log.error("Error while storing {} file {} for task {}", kind, safeName, taskId, e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }

        TaskFile taskFile = taskFileRepository.findFirstByTaskIdAndKindAndFileName(taskId, kind, safeName);
        if (taskFile == null) {
            taskFile = new TaskFile();
            taskFile.setTaskId(taskId);
            taskFile.setKind(kind);
            taskFile.setFileName(safeName);
            // Headers are what contestants need locally; hand them over by default.
            taskFile.setVisibleToContestants(kind == TaskFileKind.GRADER && isHeader(safeName));
        }
        taskFile.setFileAddress(address);
        taskFile.setSizeBytes((long) content.length);
        taskFile.setUploadedAt(new Date());
        taskFileRepository.save(taskFile);
    }

    private Map<String, byte[]> singleFile(String fileName, byte[] content, AddTaskFilesResult result) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        try {
            files.put(sanitizeFileName(fileName), content);
        } catch (InformaticsServerException e) {
            result.getRejected().add(fileName);
        }
        return files;
    }

    private Map<String, byte[]> unpackZip(byte[] zipContent, AddTaskFilesResult result)
            throws InformaticsServerException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        long totalBytes = 0;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipContent))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName();
                // Take the leaf name; a nested layout carries no meaning for the compiler.
                String leaf = entryName.substring(entryName.lastIndexOf('/') + 1);
                byte[] data = zis.readAllBytes();
                totalBytes += data.length;
                if (totalBytes > MAX_UNPACKED_BYTES) {
                    throw new InformaticsServerException("taskFilesTooLarge");
                }
                try {
                    files.put(sanitizeFileName(leaf), data);
                } catch (InformaticsServerException e) {
                    result.getRejected().add(entryName);
                }
            }
        } catch (IOException e) {
            log.error("Error while reading uploaded archive", e);
            throw new InformaticsServerException("invalidArchive");
        }
        return files;
    }

    /**
     * Rejects anything that is not a plain source file name - path separators, traversal
     * segments, unknown extensions and overlong names all fail here rather than on a worker.
     */
    String sanitizeFileName(String fileName) throws InformaticsServerException {
        if (fileName == null || fileName.isBlank() || fileName.length() > MAX_FILE_NAME_LENGTH) {
            throw new InformaticsServerException("invalidTaskFileName");
        }
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")
                || fileName.startsWith(".")) {
            throw new InformaticsServerException("invalidTaskFileName");
        }
        if (!fileName.matches("[A-Za-z0-9_.\\-]+")) {
            throw new InformaticsServerException("invalidTaskFileName");
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            throw new InformaticsServerException("invalidTaskFileName");
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InformaticsServerException("unsupportedTaskFileType");
        }
        return fileName;
    }

    private boolean isZip(String fileName) {
        return fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".zip");
    }

    private boolean isHeader(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.endsWith(".h") || lower.endsWith(".hpp") || lower.endsWith(".hxx");
    }

    private TaskFile requireFile(long taskId, TaskFileKind kind, String fileName) throws InformaticsServerException {
        TaskFile taskFile = taskFileRepository.findFirstByTaskIdAndKindAndFileName(taskId, kind, fileName);
        if (taskFile == null) {
            throw new InformaticsServerException("taskFileNotFound");
        }
        return taskFile;
    }

    private File resolveExisting(TaskFile taskFile) throws InformaticsServerException {
        File file = new File(taskFile.getFileAddress());
        if (!file.isFile()) {
            log.error("Task file {} is recorded but missing at {}", taskFile.getFileName(), taskFile.getFileAddress());
            throw new InformaticsServerException("taskFileNotFound");
        }
        return file;
    }

    private String directoryFor(long taskId, TaskFileKind kind) {
        return FileUtils.buildPath(taskRoot(taskId), kind.getDirectory());
    }

    private String taskRoot(long taskId) {
        return taskDirectoryAddress.replace(":taskId", String.valueOf(taskId));
    }

    /**
     * Bumps the marker the worker uses to decide whether its cached copy of the task is stale.
     * Without this a worker keeps compiling submissions against the previous grader.
     */
    private void touchLastUpdate(long taskId) throws InformaticsServerException {
        Path root = Paths.get(taskRoot(taskId));
        try {
            Files.createDirectories(root);
            Files.writeString(root.resolve("lastUpdate"),
                    String.valueOf(System.currentTimeMillis()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not update lastUpdate marker for task {}", taskId, e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
    }

    /**
     * Names of the grader sources, in a stable order, for callers that need to know whether
     * a task links a grader at all.
     */
    public List<String> graderFileNames(long taskId) {
        List<String> names = new ArrayList<>();
        for (TaskFile file : taskFileRepository.findByTaskIdAndKindOrderByFileNameAsc(taskId, TaskFileKind.GRADER)) {
            names.add(file.getFileName());
        }
        return names;
    }
}