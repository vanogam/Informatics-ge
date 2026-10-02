package ge.freeuni.informatics.repository.plagiarism;

import ge.freeuni.informatics.common.model.plagiarism.PlagiarismJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlagiarismJobRepository extends JpaRepository<PlagiarismJob, Long> {

    @Query("""
        SELECT j FROM PlagiarismJob j
        ORDER BY j.createdAt DESC
        LIMIT :limit OFFSET :offset
    """)
    List<PlagiarismJob> findAllOrderByCreatedAtDesc(Integer offset, Integer limit);
}
