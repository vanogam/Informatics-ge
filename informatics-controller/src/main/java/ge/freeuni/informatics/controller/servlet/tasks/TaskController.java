package ge.freeuni.informatics.controller.servlet.tasks;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
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

@RestController
public class TaskController {

    @Autowired
    Logger log;

    @Autowired
    ITaskManager taskManager;

    @Value("${ge.freeuni.informatics.defaultLanguage}")
    String defaultLanguage;

    @GetMapping("/room/{id}/get-tasks")
    void getTasks(@PathVariable String id) {

    }

    @PostMapping("/save-task")
    InformaticsResponse saveTask(@RequestBody AddTaskRequest request) {
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
