package ge.freeuni.informatics.controller.servlet.tasks;

import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.AddTaskRequest;
import ge.freeuni.informatics.controller.model.GetTasksRequest;
import ge.freeuni.informatics.controller.model.InformaticsResponse;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    @Autowired
    ITaskManager taskManager;

    @GetMapping("/get-tasks")
    void getTasks(GetTasksRequest tasksRequest) {

    }

    @PostMapping("/add-task")
    InformaticsResponse addTask(@RequestParam AddTaskRequest request) {

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskType(request.getTaskType());
        taskDTO.setCode(request.getCode());
        taskDTO.setTaskScoreType(request.getTaskScoreType());
        taskDTO.setTaskScoreParameter(request.getTaskScoreParameter());
        taskDTO.setMemoryLimitMB(request.getMemoryLimitMB());
        taskDTO.setTimeLimitMillis(request.getTimeLimitMillis());
        try {
            taskManager.addTask(taskDTO, request.getContestId());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }
}
