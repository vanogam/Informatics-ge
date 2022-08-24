package ge.freeuni.informatics.repository.contest;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;

import java.util.Date;
import java.util.List;

public interface IContestRepository {

    Contest addContest(Contest contest);

    Contest getContest(Long contestId);

    List<Contest> getContests(Long roomId, String name, List<ContestStatus> statuses, Date minStartDate, Date maxStartDate);
}
