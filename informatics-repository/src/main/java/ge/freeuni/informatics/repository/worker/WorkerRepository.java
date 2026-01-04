package ge.freeuni.informatics.repository.worker;

import ge.freeuni.informatics.common.model.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByWorkerId(String workerId);
}

