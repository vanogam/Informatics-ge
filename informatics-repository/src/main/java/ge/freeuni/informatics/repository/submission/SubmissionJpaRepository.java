package ge.freeuni.informatics.repository.submission;

import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface SubmissionJpaRepository extends JpaRepository<Submission, Long> {


    List<Submission> getAllByStatusIn(Collection<SubmissionStatus> statuses);

    @Query("""
        SELECT s FROM Submission s
        WHERE (:userId IS NULL OR s.user.id = :userId)
          AND (:taskId IS NULL OR s.task.id = :taskId)
          AND (:contestId IS NULL OR s.contest.id = :contestId)
          AND (:roomId IS NULL OR s.roomId = :roomId)
          AND (:username IS NULL OR s.user.username = :username)
          AND (:language IS NULL OR s.language = :language)
          AND (:status IS NULL OR s.status = :status)
          AND (:userId IS NOT NULL OR :viewerIsAdmin = true OR s.user.role NOT LIKE '%ADMIN%')
              ORDER BY s.submissionTime DESC
              LIMIT :limit OFFSET :offset
    """)
    List<Submission> findSubmissions(Long userId, Long taskId, Long contestId, Long roomId,
                                     String username, String language, SubmissionStatus status,
                                     boolean viewerIsAdmin, Integer offset, Integer limit);

    @Query("""
        SELECT COUNT(s) FROM Submission s
        WHERE (:userId IS NULL OR s.user.id = :userId)
          AND (:taskId IS NULL OR s.task.id = :taskId)
          AND (:contestId IS NULL OR s.contest.id = :contestId)
          AND (:roomId IS NULL OR s.roomId = :roomId)
          AND (:username IS NULL OR s.user.username = :username)
          AND (:language IS NULL OR s.language = :language)
          AND (:status IS NULL OR s.status = :status)
          AND (:userId IS NOT NULL OR :viewerIsAdmin = true OR s.user.role NOT LIKE '%ADMIN%')
    """)
    long countSubmissions(Long userId, Long taskId, Long contestId, Long roomId,
                          String username, String language, SubmissionStatus status, boolean viewerIsAdmin);

    /**
     * Every submission one contestant has made to one task that has been scored, oldest first.
     *
     * <p>Used to rebuild a standings row from scratch after a re-judge. Ordered by submission time
     * because that is what decides the tie-break, so a replay reaches the same successTime the
     * contestant originally earned however often the submissions are re-judged. A null score means
     * the submission has never finished judging and has never counted towards the standings.
     */
    @Query("""
        SELECT s FROM Submission s
        WHERE s.user.id = :userId
          AND s.task.id = :taskId
          AND s.score IS NOT NULL
        ORDER BY s.submissionTime ASC, s.id ASC
    """)
    List<Submission> findScoredForReplay(Long userId, Long taskId);

    /**
     * Every SOURCE submission for one task, optionally narrowed to a set of usernames, ordered so
     * that each user's best (then latest) submission comes first within their own run of rows.
     * The caller dedupes to one submission per user by keeping the first row seen for each
     * {@code user.id} - there is no JPQL window function available here, and the ordering alone
     * makes that correct.
     */
    @Query("""
        SELECT s FROM Submission s
        WHERE s.task.id = :taskId
          AND s.kind = ge.freeuni.informatics.common.model.submission.SubmissionKind.SOURCE
          AND (:usernames IS NULL OR s.user.username IN :usernames)
        ORDER BY s.user.id ASC, s.score DESC NULLS LAST, s.submissionTime DESC
    """)
    List<Submission> findSourceSubmissionsForPlagiarism(Long taskId, Collection<String> usernames);

    /** Distinct usernames with at least one SOURCE submission to the task - for the plagiarism check's user filter. */
    @Query("""
        SELECT DISTINCT s.user.username FROM Submission s
        WHERE s.task.id = :taskId
          AND s.kind = ge.freeuni.informatics.common.model.submission.SubmissionKind.SOURCE
        ORDER BY s.user.username ASC
    """)
    List<String> findDistinctUsernamesWithSourceSubmission(Long taskId);
}
