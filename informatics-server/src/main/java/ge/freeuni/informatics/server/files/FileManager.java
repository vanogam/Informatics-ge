package ge.freeuni.informatics.server.files;

import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.annotation.MemberTaskRestricted;
import ge.freeuni.informatics.server.annotation.TeacherTaskRestricted;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.task.TaskManager;
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
import java.util.Objects;

@Component
public class FileManager {

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    @Value("${ge.freeuni.informatics.Task.statementDirectoryAddress}")
    private String statementDirectory;

    @Autowired
    private UserManager userManager;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ContestRoomJpaRepository roomRepository;

    public String saveTextSubmission(Date submissionTime,
                                     CodeLanguage language,
                                     long contestId,
                                     long taskId,
                                     String content) throws InformaticsServerException {
        String fileName = userManager.getAuthenticatedUser().username() +
                submissionTime +
                "." + language.getSuffix();

        String fileDir = submissionDirectory + "/" + contestId + "/" + taskId;
        String filePath = fileDir + "/" + fileName;
        try {
            Files.createDirectories(Paths.get(submissionDirectory));
            Files.writeString(Paths.get(filePath), content);
            return fileName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to save submission file: " + e.getMessage(), e);
        }
    }

    @TeacherTaskRestricted
    public String saveFileForStatement(long taskId, byte[] fileContent) throws IOException, InformaticsServerException {
        String statementDir = statementDirectory + "/" + taskId;

        String filename;
        do {
            filename = FileUtils.getRandomFileName(8);
        } while(Files.exists(Paths.get(statementDir + "/" + filename), new LinkOption[0]));

        Files.createDirectories(Paths.get(statementDir));
        String filePath = statementDir + "/" + filename;
        Files.write(Paths.get(filePath), fileContent, new OpenOption[0]);
        return filename;

    }

    @MemberTaskRestricted
    public byte[] getFileForStatement(long taskId, String filename) throws IOException, InformaticsServerException {
        String userDir = statementDirectory + "/" + taskId;
        String filePath = userDir + "/" + filename;
        if (!Files.exists(Paths.get(filePath))) {
            throw new InformaticsServerException("fileNotFound");
        }
        return Files.readAllBytes(Paths.get(filePath));
    }
}
