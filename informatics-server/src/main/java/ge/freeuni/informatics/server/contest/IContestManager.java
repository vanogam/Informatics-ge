package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.ContestantResult;

import java.util.Date;
import java.util.List;

public interface IContestManager {

    void createContest(ContestDTO contest) throws InformaticsServerException;

    ContestDTO getContest(Long contestId) throws InformaticsServerException;

    List<ContestDTO> getContests(Long roomId, String name, List<ContestStatus> statuses, Boolean upsolving, Date minStartDate, Date maxStartDate);

    ContestDTO updateContest(ContestDTO contest);

    void deleteContest(long contestId);

    void registerUser(long contestId) throws InformaticsServerException;

    List<ContestantResult> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException;

}
