package ge.freeuni.informatics.controller.servlet.tasks;

import ge.freeuni.informatics.common.dto.TaskFileDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.TaskFileKind;
import ge.freeuni.informatics.controller.model.AddTaskFilesResponse;
import ge.freeuni.informatics.controller.model.InformaticsResponse;
import ge.freeuni.informatics.controller.model.SetPublicTestcasesRequest;
import ge.freeuni.informatics.controller.model.TaskFilesResponse;
import ge.freeuni.informatics.controller.servlet.ServletUtils;
import ge.freeuni.informatics.server.task.ITaskFileManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Upload and management of the source files a task hands to the judge: graders that get
 * linked into every submission, and the manager that judges communication tasks.
 */
@RestController
@RequestMapping("/api")
public class TaskFileController {

    @Autowired
    Logger log;

    @Autowired
    ITaskFileManager taskFileManager;

    @Value("${ge.freeuni.informatics.maxSingleTestcaseFileMb}")
    int maxTaskFileMb;

    @PostMapping("/task/{taskId}/graders")
    ResponseEntity<AddTaskFilesResponse> addGraders(@PathVariable Long taskId,
                                                    @RequestParam("file") MultipartFile file) {
        return addFiles(taskId, TaskFileKind.GRADER, file);
    }

    @PostMapping("/task/{taskId}/checker")
    ResponseEntity<AddTaskFilesResponse> addChecker(@PathVariable Long taskId,
                                                    @RequestParam("file") MultipartFile file) {
        return addFiles(taskId, TaskFileKind.CHECKER, file);
    }

    @PostMapping("/task/{taskId}/manager")
    ResponseEntity<AddTaskFilesResponse> addManager(@PathVariable Long taskId,
                                                    @RequestParam("file") MultipartFile file) {
        return addFiles(taskId, TaskFileKind.MANAGER, file);
    }

    private ResponseEntity<AddTaskFilesResponse> addFiles(Long taskId, TaskFileKind kind, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new AddTaskFilesResponse("fileUploadError"));
        }
        if (file.getSize() > maxTaskFileMb * 1024L * 1024L) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        try {
            return ResponseEntity.ok(new AddTaskFilesResponse(
                    taskFileManager.addTaskFiles(taskId, kind, file.getBytes(), file.getOriginalFilename())));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex))
                    .body(new AddTaskFilesResponse(ex.getCode()));
        } catch (IOException ex) {
            log.error("Error during task file upload for task {}", taskId, ex);
            return ResponseEntity.badRequest().body(new AddTaskFilesResponse("fileUploadError"));
        }
    }

    @GetMapping("/task/{taskId}/files")
    ResponseEntity<TaskFilesResponse> getTaskFiles(@PathVariable Long taskId) {
        try {
            List<TaskFileDTO> files = taskFileManager.getTaskFiles(taskId);
            return ResponseEntity.ok(new TaskFilesResponse(files));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex))
                    .body(new TaskFilesResponse(ex.getCode()));
        }
    }

    @GetMapping("/task/{taskId}/file/{kind}/{fileName}")
    ResponseEntity<InputStreamResource> getTaskFile(@PathVariable Long taskId,
                                                    @PathVariable TaskFileKind kind,
                                                    @PathVariable String fileName) {
        try {
            return fileResponse(taskFileManager.getTaskFile(taskId, kind, fileName));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex)).build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/task/{taskId}/file/{kind}/{fileName}")
    ResponseEntity<InformaticsResponse> removeTaskFile(@PathVariable Long taskId,
                                                       @PathVariable TaskFileKind kind,
                                                       @PathVariable String fileName) {
        try {
            taskFileManager.removeTaskFile(taskId, kind, fileName);
            return ResponseEntity.ok(new InformaticsResponse(null));
        } catch (InformaticsServerException ex) {
            log.error("Error while deleting task file {} of task {}", fileName, taskId, ex);
            return ResponseEntity.status(ServletUtils.getResponseCode(ex))
                    .body(new InformaticsResponse(ex.getCode()));
        }
    }

    @PutMapping("/task/{taskId}/file/{kind}/{fileName}/public")
    ResponseEntity<InformaticsResponse> setFilePublic(@PathVariable Long taskId,
                                                      @PathVariable TaskFileKind kind,
                                                      @PathVariable String fileName,
                                                      @RequestBody SetPublicTestcasesRequest request) {
        try {
            taskFileManager.setFileVisibleToContestants(taskId, kind, fileName, request.status());
            return ResponseEntity.ok(new InformaticsResponse(null));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex))
                    .body(new InformaticsResponse(ex.getCode()));
        }
    }

    @GetMapping("/task/{taskId}/attachments")
    ResponseEntity<TaskFilesResponse> getAttachments(@PathVariable Long taskId) {
        try {
            return ResponseEntity.ok(new TaskFilesResponse(taskFileManager.getContestantVisibleFiles(taskId)));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex))
                    .body(new TaskFilesResponse(ex.getCode()));
        }
    }

    @GetMapping("/task/{taskId}/attachment/{fileName}")
    ResponseEntity<InputStreamResource> getAttachment(@PathVariable Long taskId, @PathVariable String fileName) {
        try {
            return fileResponse(taskFileManager.getContestantVisibleFile(taskId, fileName));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex)).build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<InputStreamResource> fileResponse(File file) throws IOException {
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(new FileInputStream(file)));
    }
}