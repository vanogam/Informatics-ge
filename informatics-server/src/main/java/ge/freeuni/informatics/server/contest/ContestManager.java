package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserSimpleDTO;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.task.ITaskManager;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.ArrayUtils;
import jakarta.annotation.security.RolesAllowed;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class ContestManager implements IContestManager {

    final IContestRoomManager contestRoomManager;

    final IUserManager userManager;

    final ContestJpaRepository contestRepository;

    final ITaskManager taskManager;

    @Autowired
    public ContestManager(IContestRoomManager contestRoomManager, IUserManager userManager, ContestJpaRepository contestRepository, ITaskManager taskManager) {
        this.contestRoomManager = contestRoomManager;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
        this.taskManager = taskManager;
    }

    @RolesAllowed({"ROLE_TEACHER", "ROLE_ADMIN"})
    @Override
    public ContestDTO createContest(ContestDTO contestDTO) throws InformaticsServerException {
        ContestRoom room = contestRoomManager.getRoom(contestDTO.getRoomId());
        long userId = userManager.getAuthenticatedUser().id();
        if (!"ADMIN".equals(userManager.getAuthenticatedUser().role()) &&
            room.getTeachers().stream().map(User::getId).noneMatch(id -> userId == id)) {
            throw new InformaticsServerException("permissionDenied");
        }
        Contest contest;
        if (contestDTO.getId() != null) {
            contest = getContestInternal(contestDTO.getId());
            updateContest(contest, contestDTO);
        } else {
            contest = ContestDTO.fromDTO(contestDTO);
            contest.setStandings(new ArrayList<>());
        }
        contest = contestRepository.saveAndPublish(contest);
        return ContestDTO.toDTO(contest);
    }

    @Override
    public ContestDTO getContest(Long contestId) throws InformaticsServerException {

        return ContestDTO.toDTO(getContestInternal(contestId));
    }

    private Contest getContestInternal(Long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        // TODO Eager fetch room
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.isMember(userManager.getAuthenticatedUser().id())) {
            throw new InformaticsServerException("permissionDenied");
        }
        return contest;
    }

    @Override
    public List<ContestDTO> getContests(Long roomId, String name, Boolean upsolving, Date minStartDate, Date maxStartDate,
                                        Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return contestRepository.findContests(roomId, name, minStartDate, maxStartDate, null, null, upsolving, pageable)
                .stream()
                .map(ContestDTO::toDTO)
                .toList();
    }

    @Override
    public ContestDTO updateContest(ContestDTO contest) {
        return ContestDTO.toDTO(contestRepository.saveAndPublish(ContestDTO.fromDTO(contest)));
    }

    @Override
    public void deleteContest(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.isMember(userManager.getAuthenticatedUser().id())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<Task> tasks = contest.getTasks();
        for (Task task : tasks) {
            taskManager.removeTask(task.getId(), contestId);
        }
        contestRepository.deleteById(contestId);
    }

    @Override
    public void registerUser(long contestId) throws InformaticsServerException {
        boolean retry = false;
        int numRetries = 5;
        do {
            Contest contest = contestRepository.getReferenceById(contestId);
            UserDTO user = userManager.getAuthenticatedUser();
            long userId = user.id();
            ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
            if (!room.isMember(userId)) {
                throw new InformaticsServerException("permissionDenied");
            }
            if (contest.getStatus() == ContestStatus.PAST) {
                throw new InformaticsServerException("contestAlreadyOver");
            }
            if (contest.getParticipants() == null) {
                contest.setParticipants(new ArrayList<>());
            }
            if (contest.getParticipants().stream().noneMatch(u -> u.getId() == userId)) {
                contest.getParticipants().add(userManager.getUser(userId));
                contest.getStandings().add(createContestantResult(contest, userId));
            }
            try {
                contestRepository.saveAndPublish(contest);
            } catch (StaleStateException | ObjectOptimisticLockingFailureException ignored) {
                retry = true;
            }

        } while (retry && --numRetries > 0);
        if (retry) {
            throw new InformaticsServerException("contestRegistrationFailed");
        }
    }

    private ContestantResult createContestantResult(Contest contest, long userId) {
        ContestantResult contestantResult = new ContestantResult();
        contestantResult.setContest(contest);
        contestantResult.setContestant(userId);
        contestantResult.setTotalScore(0f);
        contestantResult.setTaskResults(new HashMap<>());
        return contestantResult;
    }
    @Override
    public void unregisterUser(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        UserDTO user = userManager.getAuthenticatedUser();
        long userId = user.id();
        if (contest.getParticipants() == null || !contest.hasParticipant(userId)) {
            return;
        }
        if (contest.getStatus() == ContestStatus.LIVE || contest.getStatus() == ContestStatus.PAST) {
            throw new InformaticsServerException("actionNotAvailable");
        }
        contest.setParticipants(
                contest.getParticipants()
                        .stream().filter(u -> u.getId() != userId)
                        .toList()
        );
        for (ContestantResult contestantResult : contest.getStandings()) {
            if (contestantResult.getContestantId() == userId) {
                contest.getStandings().remove(contestantResult);
                break;
            }
        }
        contestRepository.save(contest);
    }

    @Override
    public List<ContestantResult> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        long userId = userManager.getAuthenticatedUser().id();
        if (!room.isMember(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<ContestantResult> fullStandings = contest.getStandings();
        return ArrayUtils.getPage(fullStandings, offset, size);
    }

    @Override
    public boolean isCurrentUserRegistered(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        long userId = userManager.getAuthenticatedUser().id();
        return contest.getParticipants().stream().map(User::getId).anyMatch(id -> id == userId);
    }

    @Override
    public List<UserSimpleDTO> getRegistrants(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (room.isOpen() || !room.isMember(userManager.getAuthenticatedUser().id())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<UserSimpleDTO> registrants = new ArrayList<>();
        for (User user : contest.getParticipants()) {
            registrants.add(UserSimpleDTO.toSimpleDTO(user));
        }
        return registrants;
    }

    private void updateContest(Contest contest, ContestDTO contestDTO) {
        contest.setId(contestDTO.getId());
        contest.setName(contestDTO.getName());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setEndDate(contestDTO.getEndDate());
        contest.setUpsolving(contestDTO.isUpsolving());
        contest.setUpsolvingAfterFinished(contestDTO.isUpsolvingAfterFinish());
        contest.setScoringType(contestDTO.getScoringType());
    }
}
