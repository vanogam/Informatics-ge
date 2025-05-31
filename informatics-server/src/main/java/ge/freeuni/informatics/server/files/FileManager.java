package ge.freeuni.informatics.server.files;

import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.server.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

@Component
public class FileManager {

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    @Autowired
    private UserManager userManager;

    public String saveTextSubmission(Date submissionTime,
                                     CodeLanguage language,
                                     long contestId,
                                     long taskCode,
                                     String content) throws InformaticsServerException {
        String fileName = userManager.getAuthenticatedUser().username() +
                submissionTime +
                "." + language.getSuffix();

        String fileDir = submissionDirectory + "/" + contestId + "/" + taskCode;
        String filePath = fileDir + "/" + fileName;
        try {
            Files.createDirectories(Paths.get(submissionDirectory));
            Files.writeString(Paths.get(filePath), content);
            return fileName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to save submission file: " + e.getMessage(), e);
        }
    }
}
