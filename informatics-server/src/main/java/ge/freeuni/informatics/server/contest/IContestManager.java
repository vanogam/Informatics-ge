package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserSimpleDTO;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.ContestantResult;

import java.util.Date;
import java.util.List;

public interface IContestManager {

    ContestDTO createContest(Long roomId, ContestDTO contest) throws InformaticsServerException;

    ContestDTO getContest(Long contestId,
                          boolean loadParticipants,
                          boolean loadTasks,
                          boolean loadStandings,
                          boolean loadUpsolvingStandings
    ) throws InformaticsServerException;

    List<ContestDTO> getContests(Long roomId, String name, Boolean upsolving, Date minStartDate, Date maxStartDate, Integer page, Integer size) throws InformaticsServerException;

    ContestDTO updateContest(ContestDTO contest);

    ContestDTO modifyContest(Long contestId, ContestDTO contest) throws InformaticsServerException;

    void deleteContest(long contestId) throws InformaticsServerException;

    void registerUser(long contestId) throws InformaticsServerException;

    void unregisterUser(long contestId) throws InformaticsServerException;

    List<ContestantResult> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException;

    boolean isCurrentUserRegistered(long contestId) throws InformaticsServerException;

    List<UserSimpleDTO> getRegistrants(long contestId) throws InformaticsServerException;
}
