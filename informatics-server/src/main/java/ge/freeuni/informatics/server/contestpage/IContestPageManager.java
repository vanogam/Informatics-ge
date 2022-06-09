package ge.freeuni.informatics.server.contestpage;

import ge.freeuni.informatics.model.entity.contest.Contest;
import ge.freeuni.informatics.model.entity.contest.ContestStatus;

import java.util.List;

public interface IContestPageManager {

    void createContest(Contest contest);

    List<Contest> getContests(Long roomId, String name, List<ContestStatus> statuses);

    void deleteContest(long contestId);

    void registerUser(long userId, long contestId);

}
