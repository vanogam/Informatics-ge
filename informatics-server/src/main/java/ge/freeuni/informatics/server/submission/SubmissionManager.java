package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.submission.ISubmissionRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static ge.freeuni.informatics.common.model.contestroom.ContestRoom.GLOBAL_ROOM_ID;

@Service
public class SubmissionManager implements ISubmissionManager {

    private final ISubmissionRepository submissionRepository;

    private final IUserManager userManager;

    private final ContestJpaRepository contestRepository;

    private final IContestRoomManager roomManager;

    @Autowired
    public SubmissionManager(ISubmissionRepository submissionRepository,
                             IUserManager userManager,
                             ContestJpaRepository contestRepository,
                             IContestRoomManager roomManager) {
        this.submissionRepository = submissionRepository;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
        this.roomManager = roomManager;
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
    public void registerSubmission(Long submissionId, Long cmsId) {
        submissionRepository.registerSubmission(submissionId, cmsId);
    }
}
