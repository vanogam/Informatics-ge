package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.submission.ISubmissionRepository;
import ge.freeuni.informatics.repository.task.ITaskRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class SubmissionManager implements ISubmissionManager {

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    private final ISubmissionRepository submissionRepository;

    private final IUserManager userManager;

    private final IJudgeIntegration judgeIntegration;

    private final ITaskRepository taskRepository;

    private final Logger log;

    @Autowired
    public SubmissionManager(ISubmissionRepository submissionRepository, IUserManager userManager, IJudgeIntegration judgeIntegration, ITaskRepository taskRepository, Logger log) {
        this.submissionRepository = submissionRepository;
        this.userManager = userManager;
        this.judgeIntegration = judgeIntegration;
        this.taskRepository = taskRepository;
        this.log = log;
    }

    @Override
    @Secured("STUDENT")
    public void addSubmissionViaText(Submission submission, String text) throws InformaticsServerException {
        User user = userManager.getUser(submission.getUserId());
        if (!userManager.getAuthenticatedUser().getId().equals(user.getId())) {
            throw new InformaticsServerException("Can't submit for selected user");
        }
        CodeLanguage language = CodeLanguage.valueOf(submission.getLanguage());
        submission = submissionRepository.addSubmission(submission);

        submission.setFileName(submission.getId() + "." + language.getSuffix());
        String path = FileUtils.buildPath(submissionDirectory,
                user.getId().toString(),
                submission.getFileName());
        String submissionFolder = FileUtils.buildPath(submissionDirectory, user.getId().toString());
        File submissionFile = new File(path);
        try {
            Files.createDirectories(Paths.get(submissionFolder));
            if (!submissionFile.createNewFile()) {
                throw new IOException("Could not create file");
            }
            OutputStream os = new FileOutputStream(submissionFile);
            os.write(text.getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (FileNotFoundException ex) {
            log.error("Problem creating submission file", ex);
            throw new InformaticsServerException("Problem creating submission file", ex);
        } catch (IOException ex) {
            log.error("Error writing submission", ex);
            throw new InformaticsServerException("Error writing submission", ex);
        }
        submission.setFileName(path);
        submission.setStatus(SubmissionStatus.IN_QUEUE);
        submissionRepository.addSubmission(submission);
        judgeIntegration.addSubmission(taskRepository.getTask(submission.getTaskId()), submission);
    }

    @Override
    public void registerSubmission(Long submissionId, Long cmsId) {
        submissionRepository.registerSubmission(submissionId, cmsId);
    }

}
