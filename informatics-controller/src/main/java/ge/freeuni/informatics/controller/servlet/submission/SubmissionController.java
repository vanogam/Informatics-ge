package ge.freeuni.informatics.controller.servlet.submission;

import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.server.files.FileManager;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class SubmissionController {
    final IUserManager userManager;

    final ISubmissionManager submissionManager;

    FileManager fileManager;

    @Autowired
    public SubmissionController(IUserManager userManager, ISubmissionManager submissionManager, FileManager fileManager) {
        this.userManager = userManager;
        this.submissionManager = submissionManager;
        this.fileManager = fileManager;
    }

    @GetMapping("/languages")
    public ResponseEntity<GetLanguagesResponse> getLanguages() {
        GetLanguagesResponse response = new GetLanguagesResponse();
        response.setLanguages(new ArrayList<>());
        for (CodeLanguage language : CodeLanguage.values()) {
            response.getLanguages().add(new CodeLanguageDTO(language.toString(), language.getDescription()));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<InformaticsResponse> submit(@RequestBody TextSubmitRequest request) {
        InformaticsResponse response = new InformaticsResponse();

        SubmissionDTO submissionDTO;
        try {
            submissionDTO = new SubmissionDTO(
                    request.getLanguage().toString(),
                    userManager.getAuthenticatedUser().username(),
                    request.getContestId(),
                    request.getTaskId(),
                    new Date(),
                    fileManager.saveTextSubmission(
                            new Date(),
                            CodeLanguage.valueOf(request.getLanguage().toString()),
                            request.getContestId(),
                            request.getTaskId(),
                            request.getSubmissionText())
                    );
        } catch (InformaticsServerException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new InformaticsResponse());
        }

        try {
            submissionManager.addSubmission(submissionDTO);
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException e) {
            response.setMessage(e.getCode());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
