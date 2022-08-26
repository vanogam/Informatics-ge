package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestCase;

import java.util.List;

public interface ITaskRepository {

    void addTask(Task task);

    void saveTestcase(TestCase testCase);

    Task getTask(int taskId);

}
