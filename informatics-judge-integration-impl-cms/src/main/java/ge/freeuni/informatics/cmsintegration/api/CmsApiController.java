package ge.freeuni.informatics.cmsintegration.api;

import ge.freeuni.informatics.cmsintegration.manager.ICmsApiManager;
import ge.freeuni.informatics.cmsintegration.model.RegisterRequest;
import ge.freeuni.informatics.cmsintegration.model.SubmissionCompilationResultRequest;
import ge.freeuni.informatics.cmsintegration.model.SubmissionResultRequest;
import ge.freeuni.informatics.cmsintegration.model.SubmissionTestRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/cms-api")
@RolesAllowed({"ROLE_CMS"})
public class CmsApiController {

    @Autowired
    ICmsApiManager cmsApiManager;

    @PostMapping("/register-task")
    public void registerTask(@RequestBody RegisterRequest request) {
        cmsApiManager.registerTask(request.getCmsID(), request.getAppID());
    }

    @PostMapping("/register-submission")
    public void registerSubmission(@RequestBody RegisterRequest request){
        cmsApiManager.registerSubmission(request.getCmsID(), request.getAppID());
    }

    @PostMapping("/submission-compilation-result")
    public void submissionCompilationResult(@RequestBody SubmissionCompilationResultRequest request) {
        cmsApiManager.setSubmissionCompilationResult(request.getCmsID(), request.getResult(), request.getMessage());
    }

    @PostMapping("/submission-result")
    public void submissionResult(@RequestBody SubmissionResultRequest request) {
        cmsApiManager.setSubmissionResult(request.getCmsID(), Float.valueOf(request.getScore()), request.getSubmissionResult());
    }

    @PostMapping("/update-submission-test")
    public void updateSubmissionTest(@RequestBody SubmissionTestRequest request) {
        cmsApiManager.setSubmissionTest(request.getCmsID(), Integer.valueOf(request.getTestNumber()));
    }
}
