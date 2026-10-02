package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.PlagiarismComparisonDTO;

import java.util.List;

public class PlagiarismComparisonListResponse extends InformaticsResponse {

    private List<PlagiarismComparisonDTO> comparisons;

    private long totalCount;

    public PlagiarismComparisonListResponse() {
    }

    public List<PlagiarismComparisonDTO> getComparisons() {
        return comparisons;
    }

    public void setComparisons(List<PlagiarismComparisonDTO> comparisons) {
        this.comparisons = comparisons;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
