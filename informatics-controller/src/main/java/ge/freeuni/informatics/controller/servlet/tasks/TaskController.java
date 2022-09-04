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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

@RestController
public class TaskController {

    @Autowired
    Logger log;

    @Autowired
    ITaskManager taskManager;

    @Value("${ge.freeuni.informatics.defaultLanguage}")
    String defaultLanguage;

    @GetMapping("/room/{id}/tasks")
    GetTasksResponse getTasks(@PathVariable Long id, PagingRequest request) {
        try {
            if (request == null) {
                request = new PagingRequest();
            }
            List<TaskInfo> taskInfos = taskManager.getUpsolvingTasks(id, request.getOffset(), request.getLimit());
            GetTasksResponse response = new GetTasksResponse("SUCCESS", null);
            response.setTasks(taskInfos);
            return response;
        } catch (InformaticsServerException ex) {
            return new GetTasksResponse("FAIL", ex.getCode());
        }
    }

    @GetMapping("/contest/{id}/tasks")
    GetTasksResponse getContestTasks(@PathVariable Long id, PagingRequest request) {
        try {
            if (request == null) {
                request = new PagingRequest();
            }
            List<TaskInfo> taskInfos = taskManager.getContestTasks(id, request.getOffset(), request.getLimit());
            GetTasksResponse response = new GetTasksResponse("SUCCESS", null);
            response.setTasks(taskInfos);
            return response;
        } catch (InformaticsServerException ex) {
            return new GetTasksResponse("FAIL", ex.getCode());
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

    @PostMapping("/save-task")
    SaveTaskResponse saveTask(@RequestBody AddTaskRequest request) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(request.getTaskId());
        taskDTO.setTaskType(request.getTaskType());
        taskDTO.setContestId(Long.valueOf(request.getContestId()));
        taskDTO.setCode(request.getCode());
        taskDTO.setTitle(request.getTitle());
        taskDTO.setTaskScoreType(request.getTaskScoreType());
        taskDTO.setTaskScoreParameter(request.getTaskScoreParameter());
        taskDTO.setMemoryLimitMB(request.getMemoryLimitMB());
        taskDTO.setTimeLimitMillis(request.getTimeLimitMillis());
        taskDTO.setInputTemplate(request.getInputTemplate());
        taskDTO.setOutputTemplate(request.getOutputTemplate());
        try {
            return new SaveTaskResponse("SUCCESS", null, taskManager.addTask(taskDTO, request.getContestId()));
        } catch (InformaticsServerException ex) {
            return new SaveTaskResponse("FAIL", ex.getCode(), null);
        }
    }

    @PostMapping("/add-testcase")
    InformaticsResponse addTestcase(@RequestParam MultipartFile[] files, @RequestParam Integer testId, @RequestParam Integer taskId){
        if (files.length != 2) {
            return new InformaticsResponse("FAIL", "incorrectNumberOfFiles");
        }
        byte[] input = {}, output = {};
        for (MultipartFile file : files) {
            try {
                if (file.getName().equals("input")) {
                    input = file.getBytes();
                } else {
                    output = file.getBytes();
                }
            } catch (IOException ex) {
                log.error("Error during file upload", ex);
                return new InformaticsResponse("FAIL", "fileUploadError");
            }
        }
        try {
            taskManager.addTestcase(taskId, testId, input, output);
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/add-testcases")
    InformaticsResponse addTestcases(@ModelAttribute AddTestcasesRequest request){
        try {
            taskManager.addTestcases(request.getTaskId(), request.getFile().getBytes());
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        } catch (IOException ex) {
            log.error("Error during file upload", ex);
            return new InformaticsResponse("FAIL", "fileUploadError");
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @PostMapping("/upload-statement")
    InformaticsResponse uploadStatement(@RequestParam MultipartFile statement, @RequestParam Integer taskId, @RequestParam LanguageDTO language) {
        try {
            taskManager.addStatement(taskId, statement.getBytes(), Language.valueOf(language.name()));
        } catch (InformaticsServerException ex) {
            return new InformaticsResponse("FAIL", ex.getCode());
        } catch (IOException ex) {
            log.error("Error during file upload", ex);
            return new InformaticsResponse("FAIL", "fileUploadError");
        }
        return new InformaticsResponse("SUCCESS", null);
    }

    @GetMapping(value = "/statements/{task_id}/{language}", produces = MediaType.APPLICATION_PDF_VALUE)
    byte[] getStatement(@PathVariable(required = false) LanguageDTO language,
                        @PathVariable Integer task_id) {
        if (language == null) {
            language = LanguageDTO.valueOf(defaultLanguage);
        }
        try {
            File file = taskManager.getStatement(task_id, Language.valueOf(language.name()));
            return Files.readAllBytes(file.toPath());
        } catch (InformaticsServerException | IOException ex) {
            log.error("Error during sending statement", ex);
            return null;
        }
    }
}
