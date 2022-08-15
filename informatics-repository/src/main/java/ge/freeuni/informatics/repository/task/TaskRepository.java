package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

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
    public Task getTask(int taskId) {
        return em.find(Task.class, taskId);
    }
}
