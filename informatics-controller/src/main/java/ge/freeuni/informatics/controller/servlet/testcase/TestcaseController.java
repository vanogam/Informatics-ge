package ge.freeuni.informatics.controller.servlet.testcase;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.controller.servlet.ServletUtils;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class TestcaseController {

    @Autowired
    Logger log;

    @Autowired
    ITaskManager taskManager;

    @PostMapping("/task/{taskId}/testcases")
    ResponseEntity<AddTestcasesResponse> addTestcases(@RequestParam Long taskId, @ModelAttribute AddTestcasesRequest request) {
        try {
            return ResponseEntity.ok(new AddTestcasesResponse(taskManager.addTestcases(taskId, request.getFile().getBytes())));
        } catch (InformaticsServerException e) {
            return ResponseEntity.status(ServletUtils.getResponseCode(e))
                    .body(new AddTestcasesResponse(e.getCode()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new AddTestcasesResponse("fileUploadError"));
        }
    }

    @GetMapping("/task/{taskId}/testcase/{testKey}")
    ResponseEntity<InputStreamResource> getSingleTestcase(@PathVariable Long taskId, @PathVariable String testKey) {
        try {
            testKey = ServletUtils.sanitizeTestKey(testKey);
            File file = taskManager.getTestcaseZip(taskId, testKey);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(new FileInputStream(file)));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex)).build();
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
            return ResponseEntity.status(ServletUtils.getResponseCode(ex)).build();
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/task/{taskId}/testcase")
    ResponseEntity<AddTestcasesResponse> addSingleTestcase(@PathVariable Long taskId, @ModelAttribute AddSingleTestcaseRequest request) {
        try {
            return ResponseEntity.ok(new AddTestcasesResponse(taskManager.addTestcase(taskId, request.getInputFile().getBytes(), request.getOutputFile().getBytes(),
                    request.getInputFile().getOriginalFilename(), request.getOutputFile().getOriginalFilename()
            )));
        } catch (InformaticsServerException ex) {
            return ResponseEntity.status(ServletUtils.getResponseCode(ex)).build();
        } catch (IOException ex) {
            log.error("Error during file upload", ex);
            return ResponseEntity.internalServerError().build();
        }

    }

    @PutMapping("/task/{taskId}/testcases/{testKey}/public")
    ResponseEntity<InformaticsResponse> setPublicTestcases(@PathVariable Long taskId, @PathVariable String testKey, @RequestBody SetPublicTestcasesRequest request) {
        try {
            taskManager.setPublicTestcase(taskId, testKey, request.status());
        } catch (InformaticsServerException ex) {
            log.error("Error during setting public testcases", ex);
            return ResponseEntity.badRequest().body(new InformaticsResponse(ex.getCode()));
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/task/{taskId}/testcase/{testKey}")
    ResponseEntity<InformaticsResponse> deleteSingleTestcase(@PathVariable Long taskId, @PathVariable String testKey) {
        try {
            taskManager.removeTestCase(taskId, testKey);
        } catch (InformaticsServerException ex) {
            log.error("Error during deleting testcase", ex);
            return ResponseEntity.badRequest().body(new InformaticsResponse(ex.getCode()));
        }
        return ResponseEntity.ok(new InformaticsResponse(null));
    }
}

