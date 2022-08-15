package ge.freeuni.informatics.cmsintegration.api;

import ge.freeuni.informatics.cmsintegration.manager.ICmsApiManager;
import ge.freeuni.informatics.cmsintegration.model.RegisterRequest;
import ge.freeuni.informatics.cmsintegration.model.SubmissionCompilationResultRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms-api")
@Secured(value = "CMS")
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
    public void submissionResult(@RequestParam Integer cmsID, @RequestParam String score, @RequestParam String[] submissionResult) {

    }

    @PostMapping("/update-submission-test")
    public void updateSubmissionTest(@RequestParam Integer cmsID, @RequestParam String testNumber) {

    }
}
