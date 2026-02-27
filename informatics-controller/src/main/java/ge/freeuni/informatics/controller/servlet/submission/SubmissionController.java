package ge.freeuni.informatics.controller.servlet.submission;

import ge.freeuni.informatics.common.dto.UserProblemDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.files.FileManager;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    public ResponseEntity<SubmitResponse> submit(@RequestBody TextSubmitRequest request) {
        SubmitResponse response = new SubmitResponse();

        SubmissionDTO submissionDTO;
        try {
            submissionDTO = new SubmissionDTO(
                    request.getLanguage().toString(),
                    userManager.getAuthenticatedUser().username(),
                    request.getContestId(),
                    request.getTaskId(),
                    new Date(),
                    fileManager.saveTextSubmission(
                            request.getTaskId(),
                            new Date(),
                            CodeLanguage.valueOf(request.getLanguage().toString()),
                            request.getSubmissionText())
                    );
        } catch (InformaticsServerException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new SubmitResponse());
        }

        try {
            Long submissionId = submissionManager.addSubmission(submissionDTO);
            response.setSubmissionId(submissionId);
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException e) {
            response.setMessage(e.getCode());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{userId}/submissions")
    public ResponseEntity<List<SubmissionDTO>> getUserSubmissions(
            @PathVariable Long userId,
            @RequestParam(required = false) Long contestId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        try {
            List<SubmissionDTO> submissions = submissionManager.filter(userId, null, contestId, roomId, offset, limit);
            return ResponseEntity.ok(submissions);
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/username/{username}/submissions")
    public ResponseEntity<SubmissionListResponse> getUserSubmissionsByUsername(
            @PathVariable String username,
            @RequestParam(required = false) Long contestId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        try {
            User user = userManager.getUserByUsername(username);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            List<SubmissionDTO> submissions = submissionManager.filter(user.getId(), null, contestId, roomId, offset, limit);
            SubmissionListResponse response = new SubmissionListResponse("SUCCESS", null);
            response.setSubmissions(submissions);
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/{userId}/problems/{status}")
    public ResponseEntity<List<UserProblemDTO>> getUserProblems(
            @PathVariable Long userId,
            @PathVariable String status) {
        try {
            ProblemAttemptStatus attemptStatus = ProblemAttemptStatus.valueOf(status.toUpperCase());
            List<UserProblemDTO> problems = submissionManager.getUserProblems(userId, attemptStatus);
            return ResponseEntity.ok(problems);
        } catch (IllegalArgumentException | InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
