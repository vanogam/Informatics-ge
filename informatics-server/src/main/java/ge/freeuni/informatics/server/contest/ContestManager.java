package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.repository.contest.IContestRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ContestManager implements IContestManager {

    final IContestRoomManager contestRoomManager;

    final IUserManager userManager;

    final IContestRepository contestRepository;

    @Autowired
    public ContestManager(IContestRoomManager contestRoomManager, IUserManager userManager, IContestRepository contestRepository) {
        this.contestRoomManager = contestRoomManager;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
    }

    @Secured({"TEACHER", "ADMIN"})
    @Override
    public void createContest(ContestDTO contestDTO) throws InformaticsServerException {
        Contest contest = ContestDTO.fromDTO(contestDTO);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.getTeachers().contains(UserDTO.fromDTO(userManager.getAuthenticatedUser()))) {
            throw new InformaticsServerException("This user can not create contest in this room");
        }
        contestRepository.addContest(contest);
    }

    @Override
    public ContestDTO getContest(Long contestId) {
        return ContestDTO.toDTO(contestRepository.getContest(contestId));
    }

      @Override
    public List<ContestDTO> getContests(Long roomId, String name, List<ContestStatus> statuses, Date minStartDate, Date maxStartDate) {
        return null;
    }

    @Override
    public void updateContest(ContestDTO contest) {
        contestRepository.addContest(ContestDTO.fromDTO(contest));
    }

    @Override
    public void deleteContest(long contestId) {

    }

    @Override
    public void registerUser(long userId, long contestId) {

    }
}
