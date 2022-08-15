package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.submission.ISubmissionRepository;
import ge.freeuni.informatics.repository.task.ITaskRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
public class SubmissionManager implements ISubmissionManager {

    private final static String SUBMISSION_DIRECTORY = "/informaticsFiles/submissions";

    private final ISubmissionRepository submissionRepository;

    private final IUserManager userManager;

    private final IJudgeIntegration judgeIntegration;

    private final ITaskRepository taskRepository;

    @Autowired
    public SubmissionManager(ISubmissionRepository submissionRepository, IUserManager userManager, IJudgeIntegration judgeIntegration, ITaskRepository taskRepository) {
        this.submissionRepository = submissionRepository;
        this.userManager = userManager;
        this.judgeIntegration = judgeIntegration;
        this.taskRepository = taskRepository;
    }

    @Override
    @Secured("STUDENT")
    public void addSubmissionViaText(Submission submission, String text) throws InformaticsServerException {
        User user = userManager.getUser(submission.getUserId());
        if (!userManager.getAuthenticatedUser().getId().equals(user.getId())) {
            throw new InformaticsServerException("Can't submit for selected user");
        }
        CodeLanguage language = CodeLanguage.valueOf(submission.getLanguage());
        submission.setFileName(submission.getId() + "." + language.getSuffix());
        String path = FileUtils.buildPath(SUBMISSION_DIRECTORY,
                user.getId().toString(),
                submission.getFileName());
        File submissionFile = new File(path);
        try {
            OutputStream os = new FileOutputStream(submissionFile);
            os.write(text.getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (FileNotFoundException e) {
            throw new InformaticsServerException("Problem creating submission file", e);
        } catch (IOException e) {
            throw new InformaticsServerException("Error writing submission", e);
        }
        submission.setFileName(path);
        judgeIntegration.addSubmission(taskRepository.getTask(submission.getTaskId()), submission);
        submissionRepository.addSubmission(submission);
    }

    @Override
    public void registerSubmission(Long submissionId, Long cmsId) {
        submissionRepository.registerSubmission(submissionId, cmsId);
    }

}
