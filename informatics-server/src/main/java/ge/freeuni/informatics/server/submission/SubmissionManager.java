package ge.freeuni.informatics.server.submission;

import ge.freeuni.informatics.repository.submission.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmissionManager implements ISubmissionManager {

    final SubmissionRepository submissionRepository;

    @Autowired
    public SubmissionManager(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    @Override
    public void registerSubmission(Long submissionId, Long cmsId) {
        submissionRepository.registerSubmission(submissionId, cmsId);
    }

}
