package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.repository.task.ITaskRepository;
import ge.freeuni.informatics.server.contest.IContestManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class TaskManager implements ITaskManager {

    @Autowired
    ITaskRepository taskRepository;

    @Autowired
    IContestManager contestManager;

    @Autowired
    IJudgeIntegration judgeIntegration;

    @Override
    public void addTask(TaskDTO taskDTO, long contestId) throws InformaticsServerException {
        Task task = TaskDTO.fromDTO(taskDTO);
        taskRepository.addTask(task);
        ContestDTO contestDTO = contestManager.getContest(contestId);
        contestDTO.getTasks().add(TaskDTO.toDTO(task));
        contestManager.updateContest(contestDTO);
        judgeIntegration.addTask(TaskDTO.toDTO(task));
    }

    @Override
    public void removeTask(long taskId, long contest) {

    }

    @Override
    public void addStatement(long taskId, File statement, Language language) {

    }

    @Override
    public void addTestcases(long taskId, File testsZip) {

    }

    @Override
    public void addManager(long taskId, File manager) {

    }

    @Override
    public void removeManager(long taskId, String managerName) {

    }

    @Override
    public void removeTestCase(long taskId, long testcaseId) {

    }

    @Override
    public void updateTitle(String name, Language language) {

    }
}
