package ge.freeuni.informatics.server.files;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.TestKeys;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.annotation.MemberTaskRestricted;
import ge.freeuni.informatics.server.annotation.PostIdAuthorRestricted;
import ge.freeuni.informatics.server.annotation.RoomMemberRestricted;
import ge.freeuni.informatics.server.annotation.TeacherTaskRestricted;
import ge.freeuni.informatics.server.user.UserManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class FileManager {

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    @Value("${ge.freeuni.informatics.Task.statementDirectoryAddress}")
    private String statementDirectory;

    @Value("${ge.freeuni.informatics.Task.postDirectoryAddress}")
    private String postDirectory;

    @Autowired
    private UserManager userManager;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ContestRoomJpaRepository roomRepository;

    @MemberTaskRestricted
    public String saveTextSubmission(long taskId,
                                     Date submissionTime,
                                     CodeLanguage language,
                                     String content) throws InformaticsServerException {
        String fileName = userManager.getAuthenticatedUser().username() +
                submissionTime.getTime() +
                "." + language.getSuffix();

        String fileDir = submissionDirectory.replace(":taskId", String.valueOf(taskId));
        String filePath = fileDir + "/" + fileName;
        try {
            Files.createDirectories(Paths.get(fileDir));
            Files.createFile(Paths.get(filePath));
            Files.writeString(Paths.get(filePath), content);
            return fileName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to save submission file: " + e.getMessage(), e);
        }
    }

    /**
     * Stores the output files of an output-only submission: one file per test key, in a directory
     * of their own.
     *
     * <p>A source submission is a single file, so {@link Submission#getFileName()} names it
     * directly; here the same field names the directory instead, and each file inside it is named
     * for the test it answers. That keeps the worker's job simple - it asks for one test's output
     * by key - and keeps a contestant's uploads for one submission together.
     *
     * @param outputsByTestKey the uploaded content keyed by test, already mapped and validated by
     *                         the caller
     * @return the directory name, to be stored as the submission's file name
     */
    @MemberTaskRestricted
    public String saveOutputSubmission(long taskId,
                                       Date submissionTime,
                                       Map<String, byte[]> outputsByTestKey) throws InformaticsServerException {
        String directoryName = userManager.getAuthenticatedUser().username() + submissionTime.getTime();
        Path baseDir = Paths.get(submissionDirectory.replace(":taskId", String.valueOf(taskId)))
                .resolve(directoryName)
                .normalize()
                .toAbsolutePath();
        try {
            Files.createDirectories(baseDir);
            for (Map.Entry<String, byte[]> output : outputsByTestKey.entrySet()) {
                // The key came from the task's own testcases, but it reaches here by way of a
                // file name the contestant chose, so it is still resolved defensively.
                Path file = baseDir.resolve(output.getKey()).normalize().toAbsolutePath();
                if (!file.getParent().equals(baseDir)) {
                    throw InformaticsServerException.PERMISSION_DENIED;
                }
                Files.write(file, output.getValue());
            }
            return directoryName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save output submission: " + e.getMessage(), e);
        }
    }

    /**
     * The output file a submission stored for one test, or null when it submitted none for it.
     */
    public byte[] getSubmittedOutput(long taskId, String directoryName, String testKey) throws IOException {
        Path baseDir = Paths.get(submissionDirectory.replace(":taskId", String.valueOf(taskId)))
                .resolve(directoryName)
                .normalize()
                .toAbsolutePath();
        Path file = baseDir.resolve(testKey).normalize().toAbsolutePath();
        if (!file.startsWith(baseDir) || !Files.exists(file)) {
            return null;
        }
        return Files.readAllBytes(file);
    }

    /** The test keys a submission uploaded outputs for, in natural test order. */
    public List<String> listSubmittedOutputs(long taskId, String directoryName) throws IOException {
        Path baseDir = Paths.get(submissionDirectory.replace(":taskId", String.valueOf(taskId)))
                .resolve(directoryName)
                .normalize()
                .toAbsolutePath();
        if (!Files.isDirectory(baseDir)) {
            return List.of();
        }
        try (Stream<Path> entries = Files.list(baseDir)) {
            return entries.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted(TestKeys.NATURAL_ORDER)
                    .toList();
        }
    }

    @TeacherTaskRestricted
    public String saveFileForStatement(long taskId, byte[] fileContent) throws IOException, InformaticsServerException {
        String statementDir = statementDirectory.replace(":taskId", String.valueOf(taskId));

        String filename;
        do {
            filename = FileUtils.getRandomFileName(8);
        } while(Files.exists(Paths.get(statementDir + "/" + filename), new LinkOption[0]));

        Files.createDirectories(Paths.get(statementDir));
        String filePath = statementDir + "/" + filename;
        Files.write(Paths.get(filePath), fileContent);
        return filename;
    }

    @PostIdAuthorRestricted
    public String saveFileForPost(long postId, byte[] fileContent) throws IOException, InformaticsServerException {
        String filename;
        do {
            filename = FileUtils.getRandomFileName(8);
        } while(Files.exists(Paths.get(postDirectory + "/" + postId + "/" + filename)));

        Files.createDirectories(Paths.get(postDirectory + "/" + postId));
        String filePath = postDirectory + "/" + postId + "/" + filename;
        Files.write(Paths.get(filePath), fileContent);
        return filename;
    }

    @MemberTaskRestricted
    public byte[] getFileForStatement(long taskId, String filename) throws IOException, InformaticsServerException {
        Path baseDir = Paths.get(statementDirectory.replace(":taskId", String.valueOf(taskId))).normalize().toAbsolutePath();
        Path resolved = baseDir.resolve(filename).normalize().toAbsolutePath();
        if (!resolved.startsWith(baseDir)) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        if (!Files.exists(resolved)) {
            throw new InformaticsServerException("fileNotFound");
        }
        return Files.readAllBytes(resolved);
    }

    @RoomMemberRestricted
    public byte[] getFileForPost(long roomId, int postId, String filename) throws IOException, InformaticsServerException {
        Path baseDir = Paths.get(postDirectory + "/" + postId).normalize().toAbsolutePath();
        Path resolved = baseDir.resolve(filename).normalize().toAbsolutePath();
        if (!resolved.startsWith(baseDir)) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        if (!Files.exists(resolved)) {
            throw new InformaticsServerException("fileNotFound");
        }
        return Files.readAllBytes(resolved);
    }
}
