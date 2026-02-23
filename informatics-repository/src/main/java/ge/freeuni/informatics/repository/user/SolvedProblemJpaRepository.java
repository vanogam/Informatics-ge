package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;
import ge.freeuni.informatics.common.model.user.SolvedProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface SolvedProblemJpaRepository extends JpaRepository<SolvedProblem, Long> {

    List<SolvedProblem> findByUserId(Long userId);

    List<SolvedProblem> findByUserIdAndStatus(Long userId, ProblemAttemptStatus status);

    long countByUserIdAndStatus(Long userId, ProblemAttemptStatus status);

    boolean existsByUserIdAndTaskId(Long userId, Long taskId);

    Optional<SolvedProblem> findByUserIdAndTaskId(Long userId, Long taskId);
}

