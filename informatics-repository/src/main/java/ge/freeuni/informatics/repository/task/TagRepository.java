package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findFirstByNameIgnoreCase(String name);
}
