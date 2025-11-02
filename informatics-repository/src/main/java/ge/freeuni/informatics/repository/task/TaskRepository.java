package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<Task, Long> {

}
