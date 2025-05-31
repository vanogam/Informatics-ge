package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.submission.ISubmissionRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static ge.freeuni.informatics.common.model.contestroom.ContestRoom.GLOBAL_ROOM_ID;

@Service
public class SubmissionManager implements ISubmissionManager {

    private final ISubmissionRepository submissionRepository;

    private final IUserManager userManager;

    private final ContestJpaRepository contestRepository;

    private final IContestRoomManager roomManager;

    private final TaskRepository taskRepository;

    @Autowired
    public SubmissionManager(ISubmissionRepository submissionRepository,
                             IUserManager userManager,
                             ContestJpaRepository contestRepository,
                             IContestRoomManager roomManager,
                             TaskRepository taskRepository) {
        this.submissionRepository = submissionRepository;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
        this.roomManager = roomManager;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException {
        if (contestId == null && roomId == null) {
            roomId = GLOBAL_ROOM_ID;
        }
        if (roomId == null) {
            Contest contest = contestRepository.getReferenceById(contestId);
            roomId = contest.getRoomId();
        }
        ContestRoom room = roomManager.getRoom(roomId);
        long currentUserId = -1L;
        try {
            currentUserId = userManager.getAuthenticatedUser().id();
        } catch (InformaticsServerException ignored) {
        }
        if (!room.isMember(currentUserId)) {
            throw new InformaticsServerException("permissionDenied");
        }

        return SubmissionDTO.toDTOs(submissionRepository.getSubmissions(userId, taskId, contestId, roomId, offset, limit));
    }

    @Override
    public void addSubmission(SubmissionDTO submissionDTO) throws InformaticsServerException {
        Submission submission = SubmissionDTO.fromDTO(submissionDTO);
        long userId = userManager.getAuthenticatedUser().id();
        submission.setUser(userManager.getUser(userId));
        CodeLanguage language = CodeLanguage.valueOf(submission.getLanguage());
        Task task = taskRepository.getReferenceById(submissionDTO.taskId());
        Contest contest = task.getContest();
        if (contest.getStatus() != ContestStatus.LIVE && !contest.isUpsolving()) {
            throw new InformaticsServerException("contestNotLive");
        }
        if (contest.getStatus() == ContestStatus.LIVE && contest.getParticipants().stream().noneMatch(u -> u.getId() == userId)) {
            throw new InformaticsServerException("notRegistered");
        }
        submission = submissionRepository.addSubmission(submission);

        submission.setStatus(SubmissionStatus.IN_QUEUE);
        submissionRepository.addSubmission(submission);
    }


    @Override
    public void registerSubmission(Long submissionId, Long cmsId) {
    }
}
