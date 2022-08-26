package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestCase;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class TaskRepository implements ITaskRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public void addTask(Task task) {
        em.persist(task);
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
