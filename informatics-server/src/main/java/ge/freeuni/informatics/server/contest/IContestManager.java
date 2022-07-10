package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

import java.util.List;

public interface IContestManager {

    void createContest(Contest contest) throws InformaticsServerException;

    List<Contest> getContests(Long roomId, String name, List<ContestStatus> statuses);

    void deleteContest(long contestId);

    void registerUser(long userId, long contestId);

}
