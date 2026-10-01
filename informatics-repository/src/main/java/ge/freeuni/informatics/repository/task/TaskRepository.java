package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findFirstByCode(String code);

    /**
     * Every task across every contest whose title matches, for the admin submissions filter's
     * problem search - capped and searched at the database rather than loaded whole, since the
     * task list isn't bounded to one contest here. Excludes a task whose contest is null (an
     * orphaned row from a deleted contest): it can never appear in a submission's contest scope,
     * so it's not a usable filter value, and {@link ge.freeuni.informatics.common.dto.TaskDTO}
     * assumes a non-null contest.
     */
    /**
     * {@code :title} is cast explicitly everywhere it's bound: left untyped, a null value here
     * makes Postgres's JDBC driver default the parameter to {@code bytea}, which then blows up
     * the moment it reaches {@code LOWER(...)} with "function lower(bytea) does not exist" - the
     * cast forces {@code text} regardless of whether the bound value is null.
     */
    @Query("""
        SELECT t FROM Task t
        WHERE t.contest IS NOT NULL
          AND (CAST(:title AS string) IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
        ORDER BY t.id
        LIMIT :limit OFFSET :offset
    """)
    List<Task> searchTasks(String title, Integer offset, Integer limit);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.contest IS NOT NULL
          AND (CAST(:title AS string) IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
    """)
    long countSearchTasks(String title);
}
