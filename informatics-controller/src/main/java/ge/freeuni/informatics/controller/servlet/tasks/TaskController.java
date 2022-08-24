package ge.freeuni.informatics.controller.servlet.tasks;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.AddTaskRequest;
import ge.freeuni.informatics.controller.model.GetTasksRequest;
import ge.freeuni.informatics.controller.model.InformaticsResponse;
import ge.freeuni.informatics.controller.model.LanguageDTO;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;

@RestController
public class TaskController {

    @Autowired
    Logger log;

    @Autowired
    ITaskManager taskManager;

    @Value("${ge.freeuni.informatics.defaultLanguage}")
    String defaultLanguage;

    @GetMapping("/get-tasks")
    void getTasks(GetTasksRequest tasksRequest) {

    }

    @PostMapping("/add-task")
    InformaticsResponse addTask(@RequestBody AddTaskRequest request) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskType(request.getTaskType());
        taskDTO.setCode(request.getCode());
        taskDTO.setTitle(request.getTitle());
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
    InformaticsResponse addTestcases(@RequestParam MultipartFile file, @RequestParam Integer taskId){
        try {
            taskManager.addTestcases(taskId, file.getBytes());
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
