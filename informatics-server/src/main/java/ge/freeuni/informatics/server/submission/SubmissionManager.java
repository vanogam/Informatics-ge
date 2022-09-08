package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.repository.contest.IContestRepository;
import ge.freeuni.informatics.repository.submission.ISubmissionRepository;
import ge.freeuni.informatics.repository.task.ITaskRepository;
import ge.freeuni.informatics.server.contest.ContestService;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class SubmissionManager implements ISubmissionManager {

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    private final ISubmissionRepository submissionRepository;

    private final IUserManager userManager;

    private final IJudgeIntegration judgeIntegration;

    private final ITaskRepository taskRepository;

    private final IContestRepository contestRepository;

    private final IContestRoomManager roomManager;

    private final ContestService contestService;

    private final Logger log;

    @Autowired
    public SubmissionManager(ISubmissionRepository submissionRepository,
                             IUserManager userManager,
                             IJudgeIntegration judgeIntegration,
                             ITaskRepository taskRepository,
                             IContestRepository contestRepository,
                             IContestRoomManager roomManager,
                             ContestService contestService,
                             Logger log) {
        this.submissionRepository = submissionRepository;
        this.userManager = userManager;
        this.judgeIntegration = judgeIntegration;
        this.taskRepository = taskRepository;
        this.contestRepository = contestRepository;
        this.contestService = contestService;
        this.roomManager = roomManager;
        this.log = log;
    }

    @Override
    public List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException {
        if (contestId == null && roomId == null) {
            throw new InformaticsServerException("invalidRequest");
        }
        boolean isContestLive = false;
        List<Long> liveContests = new ArrayList<>();
        if (roomId == null) {
            Contest contest = contestRepository.getContest(contestId);
            roomId = contest.getRoomId();
            isContestLive = contest.getStatus() == ContestStatus.LIVE;
        } else {
            liveContests = contestService.getLiveContests();
        }
        ContestRoom room = roomManager.getRoom(roomId);
        Long currentUserId = -1L;
        try {
            currentUserId = userManager.getAuthenticatedUser().getId();
        } catch (InformaticsServerException ignored) {
        }
        if (!room.isMember(currentUserId)) {
            throw new InformaticsServerException("permissionDenied");
        }

        List<SubmissionDTO> submissions = SubmissionDTO.toDTOs(submissionRepository.getSubmissions(userId, taskId, contestId, roomId, offset, limit));
        for (SubmissionDTO submissionDTO : submissions) {
            if ((isContestLive || liveContests.contains(submissionDTO.getContestId())) && submissionDTO.getUserId() != currentUserId) {
                submissionDTO.setText(null);
            }
        }
        List<SubmissionDTO> submissionDTOS = SubmissionDTO.toDTOs(submissionRepository.getSubmissions(userId, taskId, contestId, roomId, offset, limit));

        for (SubmissionDTO submissionDTO : submissionDTOS) {
            try {
                submissionDTO.setUsername(userManager.getUser(submissionDTO.getUserId()).getUsername());
            } catch (Exception ignored) {
                submissionDTO.setUsername("[DELETED]");
            }
        }

        for (SubmissionDTO submissionDTO : submissionDTOS) {
            try {
                submissionDTO.setTaskName(taskRepository.getTask(submissionDTO.getTaskId()).getTitle().get(Language.KA.name()));
                submissionDTO.setContestName(contestRepository.getContest(submissionDTO.getContestId()).getName());
            } catch (Exception ignored) {

            }
        }
        return submissionDTOS;
    }

    @Override
    @Secured("STUDENT")
    public void addSubmissionViaText(Submission submission, String text) throws InformaticsServerException {
        submission.setUserId(userManager.getAuthenticatedUser().getId());
        CodeLanguage language = CodeLanguage.valueOf(submission.getLanguage());
        Task task = taskRepository.getTask(submission.getTaskId());
        Contest contest = contestRepository.getContest(task.getContestId());
        if (contest.getStatus() != ContestStatus.LIVE && !contest.isUpsolving()) {
            throw new InformaticsServerException("contestNotLive");
        }
        if (contest.getStatus() == ContestStatus.LIVE && isNotRegistered(contest, userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("notRegistered");
        }
        submission.setRoomId(contest.getRoomId());
        submission = submissionRepository.addSubmission(submission);
        submission.setFileName(submission.getId() + "." + language.getSuffix());
        String path = FileUtils.buildPath(submissionDirectory,
                String.valueOf(submission.getUserId()),
                submission.getFileName());
        String submissionFolder = FileUtils.buildPath(submissionDirectory, String.valueOf(submission.getUserId()));
        File submissionFile = new File(path);
        try {
            Files.createDirectories(Paths.get(submissionFolder));
            if (!submissionFile.createNewFile()) {
                throw new IOException("couldNotCreateFile");
            }
            OutputStream os = new FileOutputStream(submissionFile);
            os.write(text.getBytes(StandardCharsets.UTF_8));
            os.close();
        } catch (FileNotFoundException ex) {
            log.error("Problem creating submission file", ex);
            throw new InformaticsServerException("unexpectedError", ex);
        } catch (IOException ex) {
            log.error("Error writing submission", ex);
            throw new InformaticsServerException("unexpectedError", ex);
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

    private boolean isNotRegistered(Contest contest, long userId) {
        return !contest.getParticipants().contains(userId);
    }
}
