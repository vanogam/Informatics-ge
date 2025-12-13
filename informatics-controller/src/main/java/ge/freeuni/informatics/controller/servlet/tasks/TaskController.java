package ge.freeuni.informatics.controller.servlet.tasks;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.dto.TestcaseDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Statement;
import ge.freeuni.informatics.common.model.task.TaskInfo;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.controller.servlet.ServletUtils;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    Logger log;

    @Autowired
    ITaskManager taskManager;

    @Value("${ge.freeuni.informatics.defaultLanguage}")
    String defaultLanguage;

    @GetMapping("/room/{id}/tasks")
    ResponseEntity<GetTasksResponse> getTasks(@PathVariable Long id, PagingRequest request) {
        try {
            if (request == null) {
                request = new PagingRequest();
            }
            List<TaskInfo> taskInfos = taskManager.getUpsolvingTasks(id, request.getOffset(), request.getLimit());
            GetTasksResponse response = new GetTasksResponse(null);
            response.setTasks(taskInfos);
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(new GetTasksResponse(ex.getCode()));
        }
    }

    @GetMapping("/contest/{id}/tasks")
    ResponseEntity<GetTasksResponse> getContestTasks(@PathVariable Long id, PagingRequest request) {
        try {
            if (request == null) {
                request = new PagingRequest();
            }
            List<TaskInfo> taskInfos = taskManager.getContestTasks(id, request.getOffset(), request.getLimit());
            GetTasksResponse response = new GetTasksResponse();
            response.setTasks(taskInfos);
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(new GetTasksResponse(ex.getCode()));
        }
    }

    @GetMapping("/contest/{id}/task-names")
    TaskNamesResponse getContestTaskNames(@PathVariable Long id, TaskNamesRequest request) {
        try {
            if (request.getLanguage() == null) {
                request.setLanguage(Language.KA.name());
            }
            return new TaskNamesResponse("SUCCESS", null, taskManager.getTaskNames(id, request.getLanguage()));
        } catch (InformaticsServerException ex) {
            return new TaskNamesResponse("FAIL", ex.getCode(), null);
        }
    }

    @GetMapping("/task/{id}")
    ResponseEntity<TaskDTO> getTask(@PathVariable Integer id) {
        return ResponseEntity.ok(TaskDTO.toDTO(taskManager.getTask(id)));
    }

    @PostMapping("/task")
    ResponseEntity<TaskDTO> saveTask(@RequestBody AddTaskRequest request) {
        TaskDTO taskDTO = new TaskDTO(
                request.taskId(),
                Long.valueOf(request.contestId()),
                request.code(),
                request.title(),
                request.taskType(),
                request.taskScoreType(),
                request.taskScoreParameter(),
                request.timeLimitMillis(),
                request.memoryLimitMB(),
                request.checkerType(),
                request.inputTemplate(),
                request.outputTemplate(),
                new HashMap<>(),
                new ArrayList<>(),
                null
        );
        try {
            return ResponseEntity.ok(taskManager.addTask(request.contestId(), taskDTO));
        } catch (InformaticsServerException ex) {
            log.error("Error while saving the task", ex);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/task/{taskId}/statement")
    ResponseEntity<Void> uploadStatement(@PathVariable Long taskId, @RequestBody AddStatementRequest request) {
        taskManager.addStatement(taskId, request.statement(), request.language());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/task/{taskId}/statement/{language}")
    ResponseEntity<StatementResponse> getStatement(@PathVariable(required = false) Language language,
                                        @PathVariable Integer taskId) {
        if (language == null) {
            language = Language.valueOf(defaultLanguage);
        }
        try {
            Statement statement = taskManager.getStatement(taskId, language);
            List<TestcaseDTO> publicTestcases = taskManager.getPublicTestcases(taskId);
            StatementResponse response = new StatementResponse(statement, publicTestcases);
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex)).body(new StatementResponse(ex.getCode()));
        }
    }

    @PutMapping("/contest/{contestId}/tasks/order")
    ResponseEntity<Void> updateTaskOrder(@PathVariable Long contestId, @RequestBody UpdateTaskOrderRequest request) {
        try {
            taskManager.updateTaskOrder(contestId, request.taskIds());
            return ResponseEntity.ok().build();
        } catch (InformaticsServerException ex) {
            log.error("Error while updating task order", ex);
            return ResponseEntity.badRequest().build();
        }
    }
}
