package ge.freeuni.informatics.server.submission;


import ge.freeuni.informatics.common.dto.UserProblemDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;

import java.util.List;

public interface ISubmissionManager {

    SubmissionDTO loadFullSubmission(long id) throws InformaticsServerException;

    List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException;

    Long addSubmission(SubmissionDTO submission) throws InformaticsServerException;

    List<UserProblemDTO> getUserProblems(Long userId, ProblemAttemptStatus status) throws InformaticsServerException;

}
