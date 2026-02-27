package ge.freeuni.informatics.repository.customtest;

import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomTestRunRepository extends JpaRepository<CustomTestRun, Long> {

    Optional<CustomTestRun> findByExternalKey(String externalKey);
}

