package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestCase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class TaskRepository implements ITaskRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public Task addTask(Task task) {
        return em.merge(task);
    }

    @Override
    public void saveTestcase(TestCase testCase) {
        em.merge(testCase);
    }

    @Override
    public Task getTask(int taskId) {
        return em.find(Task.class, taskId);
    }
}
