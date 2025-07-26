package ge.freeuni.informatics.server.files;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.annotation.*;
import ge.freeuni.informatics.server.user.UserManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.Date;

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
        String statementDir = statementDirectory.replace(":taskId", String.valueOf(taskId));
        String filePath = statementDir + "/" + filename;
        if (!Files.exists(Paths.get(filePath))) {
            throw new InformaticsServerException("fileNotFound");
        }
        return Files.readAllBytes(Paths.get(filePath));
    }

    @RoomMemberRestricted
    public byte[] getFileForPost(long roomId, int postId, String filename) throws IOException, InformaticsServerException {
        String filePath = postDirectory + "/" + postId + "/" + filename;
        if (!Files.exists(Paths.get(filePath))) {
            throw new InformaticsServerException("fileNotFound");
        }
        return Files.readAllBytes(Paths.get(filePath));
    }
}
