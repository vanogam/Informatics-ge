package ge.freeuni.informatics.controller.servlet.testcase;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.controller.servlet.ServletUtils;
import ge.freeuni.informatics.server.task.ITaskManager;
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

@RestController
@RequestMapping("/api")
public class TestcaseController {

    @Autowired
    Logger log;

    @Autowired
    ITaskManager taskManager;

    @Value("${ge.freeuni.informatics.maxTestcasesZipMb}")
    int maxTestcasesZipMb;

    @Value("${ge.freeuni.informatics.maxSingleTestcaseFileMb}")
    int maxSingleTestcaseFileMb;

    @PostMapping("/task/{taskId}/testcases")
    ResponseEntity<AddTestcasesResponse> addTestcases(@RequestParam Long taskId, @ModelAttribute AddTestcasesRequest request) {
        MultipartFile zip = request.getFile();
        long maxZipBytes = maxTestcasesZipMb * 1024L * 1024L;
        if (zip == null || zip.isEmpty()) {
            return ResponseEntity.badRequest().body(new AddTestcasesResponse("fileUploadError"));
        }
        if (zip.getSize() > maxZipBytes) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        try {
            return ResponseEntity.ok(new AddTestcasesResponse(taskManager.addTestcases(taskId, zip.getBytes())));
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
        MultipartFile in = request.getInputFile();
        MultipartFile out = request.getOutputFile();
        long maxPartBytes = maxSingleTestcaseFileMb * 1024L * 1024L;
        if (in == null || out == null || in.isEmpty() || out.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (in.getSize() > maxPartBytes || out.getSize() > maxPartBytes) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }
        try {
            return ResponseEntity.ok(new AddTestcasesResponse(taskManager.addTestcase(taskId, in.getBytes(), out.getBytes(),
                    in.getOriginalFilename(), out.getOriginalFilename()
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
            return ResponseEntity
                    .status(ServletUtils.getResponseCode(ex))
                    .body(new InformaticsResponse(ex.getCode()));
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/task/{taskId}/testcase/{testKey}")
    ResponseEntity<InformaticsResponse> deleteSingleTestcase(@PathVariable Long taskId, @PathVariable String testKey) {
        try {
            taskManager.removeTestCase(taskId, testKey);
        } catch (InformaticsServerException ex) {
            log.error("Error during deleting testcase", ex);
            return ResponseEntity
                    .status(ServletUtils.getResponseCode(ex))
                    .body(new InformaticsResponse(ex.getCode()));
        }
        return ResponseEntity.ok(new InformaticsResponse(null));
    }

    @DeleteMapping("/task/{taskId}/testcases")
    ResponseEntity<InformaticsResponse> deleteTestcases(@PathVariable Long taskId, @RequestBody DeleteTestcasesRequest request) {
        try {
            taskManager.removeTestcases(taskId, request.testKeys());
        } catch (InformaticsServerException ex) {
            log.error("Error during deleting testcases", ex);
            return ResponseEntity
                    .status(ServletUtils.getResponseCode(ex))
                    .body(new InformaticsResponse(ex.getCode()));
        }
        return ResponseEntity.ok(new InformaticsResponse(null));
    }
}

