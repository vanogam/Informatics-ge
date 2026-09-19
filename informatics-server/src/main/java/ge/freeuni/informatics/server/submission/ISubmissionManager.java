package ge.freeuni.informatics.server.submission;


import ge.freeuni.informatics.common.dto.OutputSubmissionDTO;
import ge.freeuni.informatics.common.dto.RejudgeResultDTO;
import ge.freeuni.informatics.common.dto.UserProblemDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.RejudgeAction;
import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;

import java.util.List;

public interface ISubmissionManager {

    SubmissionDTO loadFullSubmission(long id) throws InformaticsServerException;

    List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException;

    Long addSubmission(SubmissionDTO submission) throws InformaticsServerException;

    /**
     * Queues a submission made of the contestant's own output files rather than source code.
     *
     * @param uploadName the name the file was uploaded under; decides whether it is read as a zip
     *                   of many outputs or as one test's output
     * @param testKey    which test a single uploaded file answers; ignored for a zip, and
     *                   optional for a single file whose name already identifies its test
     * @throws InformaticsServerException if the task does not accept output submissions, or
     *                                    nothing in the upload could be matched to a test
     */
    OutputSubmissionDTO addOutputSubmission(long taskId, String uploadName, byte[] content, String testKey)
            throws InformaticsServerException;

    List<UserProblemDTO> getUserProblems(Long userId, ProblemAttemptStatus status) throws InformaticsServerException;

    /**
     * Re-judges the given submissions, in order, from the point in the judging chain the action
     * names. One result per submission: a submission that cannot be re-judged is reported as a
     * refusal rather than aborting the rest.
     *
     * @throws InformaticsServerException only when the request itself makes no sense - no action,
     *                                    no submissions, or more than can reasonably be queued
     */
    List<RejudgeResultDTO> rejudge(List<Long> submissionIds, RejudgeAction action) throws InformaticsServerException;

}
