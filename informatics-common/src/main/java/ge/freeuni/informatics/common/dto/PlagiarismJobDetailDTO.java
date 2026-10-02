package ge.freeuni.informatics.common.dto;

import java.util.List;

/** A plagiarism job's summary plus every run it has produced so far. */
public record PlagiarismJobDetailDTO(
    PlagiarismJobSummaryDTO job,
    List<PlagiarismRunDTO> runs
) {
}
