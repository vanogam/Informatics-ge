package ge.freeuni.informatics.repository.plagiarism;

import ge.freeuni.informatics.common.model.plagiarism.PlagiarismComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlagiarismComparisonRepository extends JpaRepository<PlagiarismComparison, Long> {

    @Query("""
        SELECT c FROM PlagiarismComparison c
        WHERE c.runId = :runId
        ORDER BY c.similarity DESC
        LIMIT :limit OFFSET :offset
    """)
    List<PlagiarismComparison> findByRunIdOrderBySimilarityDesc(Long runId, Integer offset, Integer limit);

    long countByRunId(Long runId);

    long countByRunIdIn(List<Long> runIds);
}
