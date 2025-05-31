package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<Task, Long> {

}
