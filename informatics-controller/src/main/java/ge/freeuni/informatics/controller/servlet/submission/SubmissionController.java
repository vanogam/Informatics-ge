package ge.freeuni.informatics.controller.servlet.submission;

import ge.freeuni.informatics.controller.model.GetLanguagesResponse;
import ge.freeuni.informatics.controller.model.InformaticsResponse;
import ge.freeuni.informatics.controller.model.CodeLanguageDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.controller.model.TextSubmitRequest;
import ge.freeuni.informatics.server.submission.ISubmissionManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;

@RestController
public class SubmissionController {
    final IUserManager userManager;

    final ISubmissionManager submissionManager;

    @Autowired
    public SubmissionController(IUserManager userManager, ISubmissionManager submissionManager) {
        this.userManager = userManager;
        this.submissionManager = submissionManager;
    }

    @GetMapping("/get-languages")
    public GetLanguagesResponse getLanguages() {
        GetLanguagesResponse response = new GetLanguagesResponse();
        response.setStatus("SUCCESS");
        response.setLanguages(new ArrayList<>());
        for (CodeLanguage language : CodeLanguage.values()) {
            response.getLanguages().add(new CodeLanguageDTO(language.toString(), language.getDescription()));
        }
        return response;
    }

    @PostMapping("/submit")
    public InformaticsResponse submit(@RequestBody TextSubmitRequest request) {
        SubmissionDTO submissionDTO = new SubmissionDTO();
        InformaticsResponse response = new InformaticsResponse();

        submissionDTO.setLanguage(request.getLanguage().toString());
        submissionDTO.setTaskId(request.getTaskId());
        submissionDTO.setSubmissionTime(new Date());
        submissionDTO.setContestId(request.getContestId());
        try {
            submissionDTO.setUserId(userManager.getAuthenticatedUser().getId());
            submissionManager.addSubmissionViaText(SubmissionDTO.fromDTO(submissionDTO), request.getSubmissionText());
            response.setStatus("SUCCESS");
            return response;
        } catch (InformaticsServerException e) {
            response.setStatus("FAIL");
            response.setStatus(e.getCode());
            return response;
        }
    }
}
