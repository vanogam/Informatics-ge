package ge.freeuni.informatics.server.plagiarism;

import ge.freeuni.informatics.common.dto.PlagiarismComparisonDTO;
import ge.freeuni.informatics.common.dto.PlagiarismComparisonDetailDTO;
import ge.freeuni.informatics.common.dto.PlagiarismJobDetailDTO;
import ge.freeuni.informatics.common.dto.PlagiarismJobSummaryDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

import java.util.List;

public interface IPlagiarismManager {

    /**
     * Starts a check: exactly one of {@code contestId}/{@code taskId} must be set. Creates the
     * job row and hands it to the background executor, returning as soon as it's queued - the
     * check itself runs asynchronously.
     *
     * @param usernames narrows the check to these users; null or empty means every user with a
     *                   submission in scope
     */
    long startJob(Long contestId, Long taskId, List<String> usernames) throws InformaticsServerException;

    List<PlagiarismJobSummaryDTO> listJobs(Integer offset, Integer limit);

    long countJobs();

    PlagiarismJobDetailDTO getJob(long jobId) throws InformaticsServerException;

    List<PlagiarismComparisonDTO> listComparisons(long runId, Integer offset, Integer limit) throws InformaticsServerException;

    long countComparisons(long runId) throws InformaticsServerException;

    PlagiarismComparisonDetailDTO getComparisonDetail(long comparisonId) throws InformaticsServerException;

    /** Distinct usernames with a SOURCE submission in the given scope, for the check's optional user filter. */
    List<String> listEligibleUsers(Long contestId, Long taskId) throws InformaticsServerException;
}
