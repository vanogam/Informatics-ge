package ge.freeuni.informatics.repository.contest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface ContestantResultJpaRepository extends JpaRepository<ContestantResult, Long> {
}
