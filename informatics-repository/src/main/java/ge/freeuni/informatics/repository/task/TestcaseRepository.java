package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TestcaseRepository extends JpaRepository<TestCase, Long> {

    @Query("""
            SELECT t FROM TestCase t WHERE
            t.taskId = :taskId AND t.key = :key
            """)
    TestCase findFirstByTaskIdAndKey(Long taskId, String key);}
