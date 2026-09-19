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
              ORDER BY s.submissionTime DESC
              LIMIT :limit OFFSET :offset
    """)
    List<Submission> findSubmissions(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit);

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
}
