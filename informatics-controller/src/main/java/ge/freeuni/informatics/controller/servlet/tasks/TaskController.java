package ge.freeuni.informatics.controller.servlet.tasks;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.TaskInfo;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
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
            return ResponseEntity.badRequest().body(new GetTasksResponse(ex.getMessage()));
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
                request.getTaskId(),
                Long.valueOf(request.getContestId()),
                request.getCode(),
                request.getTitle(),
                request.getTaskType(),
                request.getTaskScoreType(),
                request.getTaskScoreParameter(),
                request.getTimeLimitMillis(),
                request.getMemoryLimitMB(),
                request.getInputTemplate(),
                request.getOutputTemplate(),
                new HashMap<>(),
                new ArrayList<>()
        );
        try {
            return ResponseEntity.ok(taskManager.addTask(request.getContestId(), taskDTO));
        } catch (InformaticsServerException ex) {
            log.error("Error while saving the task", ex);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/task/{taskId}/testcases")
    ResponseEntity<AddTestcasesResponse> addTestcases(@RequestParam Long taskId, @ModelAttribute AddTestcasesRequest request) {
        try {
            return ResponseEntity.ok(new AddTestcasesResponse(taskManager.addTestcases(taskId, request.getFile().getBytes())));
        } catch (InformaticsServerException e) {
            return ResponseEntity.internalServerError()
                    .body(new AddTestcasesResponse(e.getCode()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new AddTestcasesResponse("fileUploadError"));
        }
    }

    @GetMapping("/task/{taskId}/testcase/{testKey}")
    ResponseEntity<InputStreamResource> getSingleTestcase(@PathVariable Long taskId, @PathVariable String testKey) {
        try {
            File file = taskManager.getTestcaseZip(taskId, testKey);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/task/{taskId}/testcases")
    ResponseEntity<InputStreamResource> getTestcases(@PathVariable Long taskId) {
        try {
            File file = taskManager.getTestcasesZip(taskId);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/task/{taskId}/testcase")
    InformaticsResponse addSingleTestcase(@PathVariable Long taskId, @ModelAttribute AddSingleTestcaseRequest request) {
        try {
            taskManager.addTestcase(taskId, request.getInputFile().getBytes(), request.getOutputFile().getBytes(),
                    request.getInputFile().getName(), request.getOutputFile().getName()
            );
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse(ex.getCode());
        } catch (IOException ex) {
            log.error("Error during file upload", ex);
            return new InformaticsResponse("fileUploadError");
        }
        return new InformaticsResponse(null);
    }

    @DeleteMapping("/task/{taskId}/testcase/{testKey}")
    ResponseEntity<InformaticsResponse> deleteSingleTestcase(@PathVariable Long taskId, @PathVariable String testKey) {
        try {
            taskManager.removeTestCase(taskId, testKey);
        } catch (InformaticsServerException ex) {
            log.error("Error during deleting testcase", ex);
            return ResponseEntity.badRequest().body(new InformaticsResponse(ex.getMessage()));
        }
        return ResponseEntity.ok(new InformaticsResponse(null));
    }

    @PostMapping("/task/{taskId}/statement")
    ResponseEntity<Void> uploadStatement(@PathVariable Long taskId, @RequestBody AddStatementRequest request) {
        taskManager.addStatement(taskId, request.statement(), request.language());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/task/{taskId}/statement/{language}", produces = MediaType.APPLICATION_PDF_VALUE)
    ResponseEntity<String> getStatement(@PathVariable(required = false) Language language,
                                        @PathVariable Integer taskId) {
        if (language == null) {
            language = Language.valueOf(defaultLanguage);
        }
        try {
            return ResponseEntity.ok(taskManager.getStatement(taskId, Language.valueOf(language.name())));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
