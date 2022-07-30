package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

import java.util.Date;
import java.util.List;

public interface IContestManager {

    void createContest(ContestDTO contest) throws InformaticsServerException;

    ContestDTO getContest(Long contestId);

    List<ContestDTO> getContests(Long roomId, String name, List<ContestStatus> statuses, Date minStartDate, Date maxStartDate);

    void updateContest(ContestDTO contest);

    void deleteContest(long contestId);

    void registerUser(long userId, long contestId);

}
