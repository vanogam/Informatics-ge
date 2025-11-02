package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.dto.SubmissionTestResultDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.judgeintegration.JudgeIntegration;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.repository.task.TestcaseRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.task.TaskManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static ge.freeuni.informatics.common.model.contestroom.ContestRoom.GLOBAL_ROOM_ID;

@Service
public class SubmissionManager implements ISubmissionManager {

    private static final List<SubmissionStatus> ONGOING_STATUSES = List.of(
            SubmissionStatus.IN_QUEUE,
            SubmissionStatus.COMPILING,
            SubmissionStatus.RUNNING
    );
    private final TestcaseRepository testcaseRepository;

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    private final SubmissionJpaRepository submissionRepository;

    private final IUserManager userManager;

    private final ContestJpaRepository contestRepository;

    private final IContestRoomManager roomManager;

    private final TaskRepository taskRepository;

    private final JudgeIntegration judgeIntegration;

    private final TaskManager taskManager;

    @Autowired
    public SubmissionManager(SubmissionJpaRepository submissionRepository,
                             IUserManager userManager,
                             ContestJpaRepository contestRepository,
                             IContestRoomManager roomManager,
                             TaskRepository taskRepository,
                             JudgeIntegration judgeIntegration,
                             TestcaseRepository testcaseRepository,
                             TaskManager taskManager) {
        this.submissionRepository = submissionRepository;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
        this.roomManager = roomManager;
        this.taskRepository = taskRepository;
        this.judgeIntegration = judgeIntegration;
        this.testcaseRepository = testcaseRepository;
        this.taskManager = taskManager;
    }

    @Override
    public SubmissionDTO loadFullSubmission(long id) throws InformaticsServerException {
        Submission submission = submissionRepository.getReferenceById(id);
        try {
            String code = Files.readString(Path.of(submissionDirectory.replace(":taskId", String.valueOf(submission.getTask().getId())) + "/" + submission.getFileName()));
            List<SubmissionTestResultDTO> testResults = submission.getSubmissionTestResults()
                    .stream()
                    .map(result -> {
                        Testcase tc = testcaseRepository.findFirstByTaskIdAndKey(submission.getTask().getId(), result.getTestKey());
                        return SubmissionTestResultDTO.toDto(result, tc.getInputSnippet(), tc.getOutputSnippet());
                    })
                    .sorted(Comparator.comparing(SubmissionTestResultDTO::testKey))
                    .toList();
            return SubmissionDTO.toDTOFull(submission, code, testResults);
        } catch (Exception e) {
            throw new InformaticsServerException("unexpectedException", e);
        }
    }

    @Override
    public List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException {
        if (contestId == null && roomId == null) {
            roomId = GLOBAL_ROOM_ID;
        }
        if (taskId != null) {
            Task task = taskRepository.getReferenceById(taskId);
            if (contestId == null) {
                contestId = task.getContest().getId();
            } else if (!task.getContest().getId().equals(contestId)) {
                throw new InformaticsServerException("taskNotInContest");
            }
        }
        Contest contest = contestRepository.getReferenceById(contestId);

        if (roomId == null) {
            roomId = contest.getRoomId();
        }
        if (!Objects.equals(contest.getRoomId(), roomId)) {
            throw new InformaticsServerException("contestNotInRoom");
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

        return submissionRepository.findSubmissions(userId, taskId, contestId, roomId, offset, limit)
                .stream()
                .map(SubmissionDTO::toDtoLight)
                .toList();
    }

    @Override
    @Transactional
    public void addSubmission(SubmissionDTO submissionDTO) throws InformaticsServerException {
        Submission submission = SubmissionDTO.fromDTO(submissionDTO);
        long userId = userManager.getAuthenticatedUser().id();
        submission.setUser(userManager.getUser(userId));
        Task task = taskRepository.getReferenceById(submissionDTO.taskId());
        Contest contest = task.getContest();
        if (contest.getStatus() != ContestStatus.LIVE && !contest.isUpsolving()) {
            throw new InformaticsServerException("contestNotLive");
        }
        if (contest.getStatus() == ContestStatus.LIVE && contest.getParticipants().stream().noneMatch(u -> u.getId() == userId)) {
            throw new InformaticsServerException("notRegistered");
        }
        submission.setRoomId(contest.getRoomId());
        submission.setStatus(SubmissionStatus.IN_QUEUE);
        submission.setTask(task);
        submission.setContest(contest);
        submission = submissionRepository.save(submission);

        judgeIntegration.addSubmission(task, submission);
    }


    @Override
    public void registerSubmission(Long submissionId, Long cmsId) {
    }
}
