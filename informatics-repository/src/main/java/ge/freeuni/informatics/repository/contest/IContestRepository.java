package ge.freeuni.informatics.repository.contest;

import ge.freeuni.informatics.common.model.contest.Contest;

import java.util.List;

public interface IContestRepository {

    void addContest(Contest contest);

    Contest getContest(Long contestId);

    List<Contest> getRoomContests(Long roomId);
}
