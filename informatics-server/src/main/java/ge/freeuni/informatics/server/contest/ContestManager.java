package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

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

    @Secured({"TEACHER", "ADMIN"})
    @Override
    public void createContest(ContestDTO contestDTO) throws InformaticsServerException {
        Contest contest = ContestDTO.fromDTO(contestDTO);
        contest.setStatus(ContestStatus.FUTURE);
        contest.setStandings(new Standings());
        contest.setVersion(1);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.getTeachers().contains(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("This user can not create contest in this room");
        }
        contestRepository.addContest(contest);
    }

    @Override
    public ContestDTO getContest(Long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (room.isMember(userManager.getAuthenticatedUser().getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        return ContestDTO.toDTO(contest);
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
        Contest contest = contestRepository.getContest(contestId);
        UserDTO user = userManager.getAuthenticatedUser();
        long userId = user.getId();
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.getParticipants().contains(userId) && !room.getTeachers().contains(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
        if (contest.getParticipants() == null) {
            contest.setParticipants(new ArrayList<>());
        }
        if (!contest.getParticipants().contains(userId)) {
            contest.getParticipants().add(userId);
            contest.getStandings().getStandings().add(new ContestantResult(contest.getScoringType(),
                    (int) userId));
        }
        contestRepository.addContest(contest);
    }

    @Override
    public void unregisterUser(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        UserDTO user = userManager.getAuthenticatedUser();
        long userId = user.getId();
        if (contest.getParticipants() == null || !contest.getParticipants().contains(userId)) {
            return;
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
}
