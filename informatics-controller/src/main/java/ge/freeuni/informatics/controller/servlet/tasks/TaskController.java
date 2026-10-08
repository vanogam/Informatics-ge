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
    ResponseEntity<GetTasksResponse> getTasks(@PathVariable Long id,
                                              @RequestParam(required = false) String tag,
                                              PagingRequest request) {
        try {
            if (request == null) {
                request = new PagingRequest();
            }
            List<TaskInfo> taskInfos = taskManager.getUpsolvingTasks(id, request.getOffset(), request.getLimit(), tag);
            GetTasksResponse response = new GetTasksResponse(null);
            response.setTasks(taskInfos);
            response.setTotalCount(taskManager.getUpsolvingTasksCount(id, tag));
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().body(new GetTasksResponse(ex.getCode()));
        }
    }

    @GetMapping("/contest/{id}/tasks")
    ResponseEntity<GetTasksResponse> getContestTasks(@PathVariable Long id,
                                                     @RequestParam(required = false) String title,
                                                     PagingRequest request) {
        try {
            if (request == null) {
                request = new PagingRequest();
            }
            List<TaskInfo> taskInfos = taskManager.getContestTasks(id, title, request.getOffset(), request.getLimit());
            GetTasksResponse response = new GetTasksResponse();
            response.setTasks(taskInfos);
            response.setTotalCount(taskManager.getContestTasksCount(id, title));
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
    ResponseEntity<?> saveTask(@RequestBody AddTaskRequest request) throws InformaticsServerException {
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
                request.numProcesses(),
                request.allowCodeSubmission(),
                request.allowOutputSubmission(),
                request.inputTemplate(),
                request.outputTemplate(),
                new HashMap<>(),
                new ArrayList<>(),
                null,
                null,
                null
        );
        try {
            return ResponseEntity.ok(taskManager.addTask(request.contestId(), taskDTO));
        } catch (InformaticsServerException ex) {
            log.error("Error while saving the task", ex);
            throw ex;
        }
    }

    @PostMapping("/task/{taskId}/statement")
    ResponseEntity<Void> uploadStatement(@PathVariable Long taskId, @RequestBody AddStatementRequest request) throws InformaticsServerException {
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

    @PostMapping("/task/{taskId}/editorial")
    ResponseEntity<Void> uploadEditorial(@PathVariable Long taskId, @RequestBody AddEditorialRequest request) throws InformaticsServerException {
        taskManager.addEditorial(taskId, request.editorial(), request.language());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/task/{taskId}/editorial/{language}")
    ResponseEntity<EditorialResponse> getEditorial(@PathVariable(required = false) Language language,
                                                    @PathVariable Long taskId) throws InformaticsServerException {
        if (language == null) {
            language = Language.valueOf(defaultLanguage);
        }
        return ResponseEntity.ok(new EditorialResponse(taskManager.getEditorial(taskId, language)));
    }

    @PutMapping("/task/{taskId}/editorial/visible")
    ResponseEntity<Void> setEditorialVisible(@PathVariable Long taskId, @RequestBody SetVisibleRequest request) throws InformaticsServerException {
        taskManager.setEditorialVisible(taskId, request.visible());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/task/{taskId}/solution")
    ResponseEntity<Void> uploadSolution(@PathVariable Long taskId, @RequestBody AddSolutionRequest request) throws InformaticsServerException {
        taskManager.addSolution(taskId, request.code(), request.language());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/task/{taskId}/solution/languages")
    ResponseEntity<SolutionLanguagesResponse> getSolutionLanguages(@PathVariable Long taskId) throws InformaticsServerException {
        return ResponseEntity.ok(new SolutionLanguagesResponse(taskManager.getSolutionLanguages(taskId)));
    }

    @GetMapping("/task/{taskId}/solution/{language}")
    ResponseEntity<SolutionResponse> getSolution(@PathVariable Long taskId, @PathVariable String language) throws InformaticsServerException {
        return ResponseEntity.ok(new SolutionResponse(taskManager.getSolution(taskId, language), language));
    }

    @DeleteMapping("/task/{taskId}/solution/{language}")
    ResponseEntity<Void> removeSolution(@PathVariable Long taskId, @PathVariable String language) throws InformaticsServerException {
        taskManager.removeSolution(taskId, language);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/task/{taskId}/solution/visible")
    ResponseEntity<Void> setSolutionVisible(@PathVariable Long taskId, @RequestBody SetVisibleRequest request) throws InformaticsServerException {
        taskManager.setSolutionVisible(taskId, request.visible());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/task/{taskId}/materials")
    ResponseEntity<TaskMaterialsResponse> getMaterialsAvailability(@PathVariable Long taskId) throws InformaticsServerException {
        var materials = taskManager.getMaterialsAvailability(taskId);
        return ResponseEntity.ok(new TaskMaterialsResponse(materials.editorialAvailable(), materials.solutionLanguages()));
    }

    @GetMapping("/task/{taskId}/tags")
    ResponseEntity<TagsResponse> getTags(@PathVariable Long taskId) throws InformaticsServerException {
        return ResponseEntity.ok(new TagsResponse(taskManager.getTags(taskId)));
    }

    @PostMapping("/task/{taskId}/tags")
    ResponseEntity<Void> addTag(@PathVariable Long taskId, @RequestBody AddTagRequest request) throws InformaticsServerException {
        taskManager.addTag(taskId, request.tag());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/task/{taskId}/tags/{tag}")
    ResponseEntity<Void> removeTag(@PathVariable Long taskId, @PathVariable String tag) throws InformaticsServerException {
        taskManager.removeTag(taskId, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tags")
    ResponseEntity<TagsResponse> getAllTags() {
        return ResponseEntity.ok(new TagsResponse(taskManager.getAllTagNames()));
    }

    @PutMapping("/contest/{contestId}/tasks/order")
    ResponseEntity<Void> updateTaskOrder(@PathVariable Long contestId, @RequestBody UpdateTaskOrderRequest request) throws InformaticsServerException {
        try {
            taskManager.updateTaskOrder(contestId, request.taskIds());
            return ResponseEntity.ok().build();
        } catch (InformaticsServerException ex) {
            log.error("Error while updating task order", ex);
            throw ex;
        }
    }
}
