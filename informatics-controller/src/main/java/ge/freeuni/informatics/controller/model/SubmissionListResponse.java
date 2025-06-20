package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.SubmissionDTO;

import java.util.List;

public class SubmissionListResponse extends InformaticsResponse {

    List<SubmissionDTO> submissions;

    public SubmissionListResponse(String status, String message) {
        super(message);
    }

    public List<SubmissionDTO> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<SubmissionDTO> submissions) {
        this.submissions = submissions;
    }
}
