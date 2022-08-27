package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestCase;

public interface ITaskRepository {

    Task addTask(Task task);

    void saveTestcase(TestCase testCase);

    Task getTask(int taskId);

}
