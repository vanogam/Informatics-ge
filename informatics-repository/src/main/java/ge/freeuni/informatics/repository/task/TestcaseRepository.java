package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestcaseRepository extends JpaRepository<TestCase, Long> {
}
