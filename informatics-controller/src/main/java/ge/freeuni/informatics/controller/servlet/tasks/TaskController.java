package ge.freeuni.informatics.controller.servlet.tasks;

import ge.freeuni.informatics.controller.model.GetTasksRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {

    @GetMapping("/get-tasks")
    void getTasks(GetTasksRequest tasksRequest) {

    }
}
