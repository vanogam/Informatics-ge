package ge.freeuni.informatics.controller.servlet.submission;

import ge.freeuni.informatics.common.dto.OutputSubmissionDTO;
import ge.freeuni.informatics.common.dto.UserProblemDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.submission.SubmissionKind;
import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.controller.servlet.ServletUtils;
import ge.freeuni.informatics.server.files.FileManager;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
                    SubmissionKind.SOURCE,
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
            return ResponseEntity.status(ServletUtils.getResponseCode(e)).body(response);
        }
    }

    /**
     * Submits the contestant's own output files for an output-only or mixed task: either one
     * test's answer, or a zip covering as many as they have solved.
     *
     * <p>Multipart rather than JSON because the payload is a file and can be large; the answer to
     * a single test may be megabytes of text, and base64 in a JSON body would inflate it further.
     */
    @PostMapping(value = "/submit/output", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OutputSubmitResponse> submitOutput(
            @RequestParam Long taskId,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) String testKey) {
        try {
            OutputSubmissionDTO result = submissionManager.addOutputSubmission(
                    taskId, file.getOriginalFilename(), file.getBytes(), testKey);

            OutputSubmitResponse response = new OutputSubmitResponse();
            response.setSubmissionId(result.submissionId());
            response.setMatchedTests(result.matchedTests());
            response.setTotalTests(result.totalTests());
            response.setUnmatchedFiles(result.unmatchedFiles());
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException e) {
            return ResponseEntity.status(ServletUtils.getResponseCode(e))
                    .body(new OutputSubmitResponse(e.getCode()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OutputSubmitResponse(InformaticsServerException.UNEXPECTED_ERROR.getCode()));
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
            response.setTotalCount(submissionManager.countFilter(user.getId(), null, contestId, roomId));
            return ResponseEntity.ok(response);
        } catch (InformaticsServerException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * The current user's own submissions across a room (used by the archive/upsolving "my
     * submissions" tab, which isn't scoped to a single contest) - mirrors
     * {@link ge.freeuni.informatics.controller.servlet.contest.ContestController#getSubmissionsList}
     * but keyed by room instead of contest.
     */
    @GetMapping("/room/{roomId}/submissions")
    public SubmissionListResponse getRoomSubmissionsList(@PathVariable Long roomId, GetSubmissionsRequest request) {
        SubmissionListResponse response = new SubmissionListResponse("SUCCESS", null);
        try {
            Long userId = userManager.getAuthenticatedUser().id();
            response.setSubmissions(submissionManager.filter(userId, request.getTaskId(), null, roomId, request.getOffset(), request.getLimit()));
            response.setTotalCount(submissionManager.countFilter(userId, request.getTaskId(), null, roomId));
        } catch (InformaticsServerException ex) {
            return new SubmissionListResponse("FAIL", ex.getCode());
        }
        return response;
    }

    /**
     * Every submission in a room, any contestant - the archive/upsolving "status" tab's
     * counterpart to {@link ge.freeuni.informatics.controller.servlet.contest.ContestController#getStatus}.
     */
    @GetMapping("/room/{roomId}/status")
    public SubmissionListResponse getRoomStatus(@PathVariable Long roomId, GetSubmissionsRequest request) {
        SubmissionListResponse response = new SubmissionListResponse("SUCCESS", null);
        try {
            response.setSubmissions(submissionManager.filter(null, request.getTaskId(), null, roomId, request.getOffset(), request.getLimit()));
            response.setTotalCount(submissionManager.countFilter(null, request.getTaskId(), null, roomId));
        } catch (InformaticsServerException ex) {
            return new SubmissionListResponse("FAIL", ex.getCode());
        }
        return response;
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
