package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserSimpleDTO;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contest.Standings;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.contest.IContestRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.task.ITaskManager;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.ArrayUtils;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ContestManager implements IContestManager {

    final IContestRoomManager contestRoomManager;

    final IUserManager userManager;

    final IContestRepository contestRepository;

    final ITaskManager taskManager;

    @Autowired
    public ContestManager(IContestRoomManager contestRoomManager, IUserManager userManager, IContestRepository contestRepository, ITaskManager taskManager) {
        this.contestRoomManager = contestRoomManager;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
        this.taskManager = taskManager;
    }

    @RolesAllowed({"ROLE_TEACHER", "ROLE_ADMIN"})
    @Override
    public ContestDTO createContest(ContestDTO contestDTO) throws InformaticsServerException {
        ContestRoom room = contestRoomManager.getRoom(contestDTO.getRoomId());
        if (!userManager.getAuthenticatedUser().getRoles().contains("ADMIN") &&
            !room.getTeachers().contains(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        Contest contest;
        if (contestDTO.getId() != null) {
            contest = getContestInternal(contestDTO.getId());
            updateContest(contest, contestDTO);
        } else {
            contest = ContestDTO.fromDTO(contestDTO);
            contest.setStatus(ContestStatus.FUTURE);
            contest.setStandings(new Standings());
        }
        contest = contestRepository.addContest(contest);
        return ContestDTO.toDTO(contest);
    }

    @Override
    public ContestDTO getContest(Long contestId) throws InformaticsServerException {

        return ContestDTO.toDTO(getContestInternal(contestId));
    }

    private Contest getContestInternal(Long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.isMember(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        return contest;
    }

    @Override
    public List<ContestDTO> getContests(Long roomId, String name, List<ContestStatus> statuses, Boolean upsolving, Date minStartDate, Date maxStartDate) {
        return ContestDTO.toDTOs(contestRepository.getContests(roomId, name, statuses, upsolving, minStartDate, maxStartDate));
    }

    @Override
    public ContestDTO updateContest(ContestDTO contest) {
        return ContestDTO.toDTO(contestRepository.addContest(ContestDTO.fromDTO(contest)));
    }

    @Override
    public void deleteContest(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.isMember(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<Task> tasks = contest.getTasks();
        for (Task task : tasks) {
            taskManager.removeTask(task.getId(), contestId);
        }
        contestRepository.deleteContest(contestId);
    }

    @Override
    public void registerUser(long contestId) throws InformaticsServerException {
        boolean retry = false;
        do {
            Contest contest = contestRepository.getContest(contestId);
            UserDTO user = userManager.getAuthenticatedUser();
            long userId = user.getId();
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
            if (!contest.getParticipants().contains(userId)) {
                contest.getParticipants().add(userId);
                contest.getStandings().getStandings().add(new ContestantResult(contest.getScoringType(),
                        (int) userId));
            }
            try {
                contestRepository.addContest(contest);
            } catch (StaleStateException | ObjectOptimisticLockingFailureException ignored) {
                retry = true;
            }

        } while (retry);
    }

    @Override
    public void unregisterUser(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        UserDTO user = userManager.getAuthenticatedUser();
        long userId = user.getId();
        if (contest.getParticipants() == null || !contest.getParticipants().contains(userId)) {
            return;
        }
        if (contest.getStatus() == ContestStatus.LIVE || contest.getStatus() == ContestStatus.PAST) {
            throw new InformaticsServerException("actionNotAvailable");
        }
        contest.getParticipants().remove(userId);
        for (ContestantResult contestantResult : contest.getStandings().getStandings()) {
            if (contestantResult.getContestantId() == userId) {
                contest.getStandings().getStandings().remove(contestantResult);
                break;
            }
        }
        contestRepository.addContest(contest);
    }

    @Override
    public List<ContestantResult> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        long userId = userManager.getAuthenticatedUser().getId();
        if (!room.isMember(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<ContestantResult> fullStandings = contest.getStandings().getStandings();
        return ArrayUtils.getPage(fullStandings, offset, size);
    }

    @Override
    public boolean isCurrentUserRegistered(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        return contest.getParticipants().contains(userManager.getAuthenticatedUser().getId());
    }

    @Override
    public List<UserSimpleDTO> getRegistrants(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (room.isOpen() || !room.isMember(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<UserSimpleDTO> registrants = new ArrayList<>();
        for (Long userId : contest.getParticipants()) {
            registrants.add(UserSimpleDTO.toSimpleDTO(userManager.getUser(userId)));
        }
        return registrants;
    }

    private void updateContest(Contest contest, ContestDTO contestDTO) {
        contest.setId(contestDTO.getId());
        contest.setName(contestDTO.getName());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setDurationInSeconds(contestDTO.getDurationInSeconds());
        contest.setUpsolving(contestDTO.isUpsolving());
        contest.setUpsolvingAfterFinished(contestDTO.isUpsolvingAfterFinish());
        contest.setScoringType(contestDTO.getScoringType());
        contest.setUpsolvingStandings(contestDTO.getUpsolvingStandings());
    }
}
