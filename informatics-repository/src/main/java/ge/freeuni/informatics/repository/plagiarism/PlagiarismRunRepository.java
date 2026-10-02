package ge.freeuni.informatics.repository.plagiarism;

import ge.freeuni.informatics.common.model.plagiarism.PlagiarismRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlagiarismRunRepository extends JpaRepository<PlagiarismRun, Long> {

    List<PlagiarismRun> findByJobIdOrderByIdAsc(Long jobId);
}
