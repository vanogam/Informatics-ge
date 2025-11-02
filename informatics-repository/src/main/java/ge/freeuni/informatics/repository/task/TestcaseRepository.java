package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Testcase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TestcaseRepository extends JpaRepository<Testcase, Long> {

    @Query("""
            SELECT t FROM Testcase t WHERE
            t.taskId = :taskId AND t.key = :key
            """)
    Testcase findFirstByTaskIdAndKey(Long taskId, String key);

    List<Testcase> findByTaskId(Long taskId);

    Optional<Testcase> findFirstByKey(String key);

    Collection<Testcase> findByTaskIdAndPublicTestcaseOrderByKey(Long taskId, boolean publicTestcase);
}
