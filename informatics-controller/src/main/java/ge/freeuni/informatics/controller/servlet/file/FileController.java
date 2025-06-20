package ge.freeuni.informatics.controller.servlet.file;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.ImageUploadResponse;
import ge.freeuni.informatics.server.files.FileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class FileController {

    @Autowired
    FileManager fileManager;

    @Value("${ge.freeuni.informatics.maxImageSizeMB}")
    Integer maxImageSize;

    @PostMapping("/task/{taskId}/image")
    ResponseEntity<ImageUploadResponse> uploadImage(@PathVariable Long taskId, @RequestParam("file") MultipartFile file) {
        if (file.getSize() > maxImageSize * 1024 * 1024) {
            return ResponseEntity.badRequest().body(new ImageUploadResponse("maxSizeExceeded"));
        }
        try {
            return ResponseEntity.ok(new ImageUploadResponse(fileManager.saveFileForStatement(taskId, file.getBytes())));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(new ImageUploadResponse(true, "uploadError"));
        } catch (InformaticsServerException e) {
            return ResponseEntity.badRequest().body(new ImageUploadResponse(true, e.getMessage()));
        }
    }

    @GetMapping("/task/{taskId}/image/{filename}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long taskId, @PathVariable String filename) {
        try {
            byte[] fileContent = fileManager.getFileForStatement(taskId, filename);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(fileContent);
        } catch (IOException | InformaticsServerException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
