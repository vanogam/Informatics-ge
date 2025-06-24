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
    public List<Submission> findSubmissions(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit);
}
