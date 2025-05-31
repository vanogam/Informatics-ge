package ge.freeuni.informatics.server.submission;


import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.Submission;

import java.util.List;

public interface ISubmissionManager {

    List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException;

    void registerSubmission(Long submissionId, Long cmsId);

}
