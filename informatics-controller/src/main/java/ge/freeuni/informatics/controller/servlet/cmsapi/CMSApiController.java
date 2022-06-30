package ge.freeuni.informatics.controller.servlet.cmsapi;

import ge.freeuni.informatics.server.submission.SubmissionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CMSApiController {

    final SubmissionManager submissionManager;

    @Autowired
    public CMSApiController(SubmissionManager submissionManager) {
        this.submissionManager = submissionManager;
    }

    @PostMapping("/api/register-submission")
    void registerSubmission(@RequestAttribute Long submissionId, @RequestAttribute Long cmsId) {
        submissionManager.registerSubmission(submissionId, cmsId);
    }

}
