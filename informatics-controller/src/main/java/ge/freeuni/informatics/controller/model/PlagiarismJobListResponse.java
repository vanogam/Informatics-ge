package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.PlagiarismJobSummaryDTO;

import java.util.List;

public class PlagiarismJobListResponse extends InformaticsResponse {

    private List<PlagiarismJobSummaryDTO> jobs;

    private long totalCount;

    public PlagiarismJobListResponse() {
    }

    public List<PlagiarismJobSummaryDTO> getJobs() {
        return jobs;
    }

    public void setJobs(List<PlagiarismJobSummaryDTO> jobs) {
        this.jobs = jobs;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
