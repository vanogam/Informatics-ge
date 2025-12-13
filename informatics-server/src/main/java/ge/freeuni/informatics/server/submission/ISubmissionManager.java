package ge.freeuni.informatics.server.submission;


import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

import java.util.List;

public interface ISubmissionManager {

    SubmissionDTO loadFullSubmission(long id) throws InformaticsServerException;

    List<SubmissionDTO> filter(Long userId, Long taskId, Long contestId, Long roomId, Integer offset, Integer limit) throws InformaticsServerException;

    void addSubmission(SubmissionDTO submission) throws InformaticsServerException;

    void registerSubmission(Long submissionId, Long cmsId);

}
