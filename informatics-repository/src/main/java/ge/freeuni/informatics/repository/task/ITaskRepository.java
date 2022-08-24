package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;

public interface ITaskRepository {

    void addTask(Task task);

    Task getTask(int taskId);
    
}
