package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.common.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface UserJpaRepository extends JpaRepository<User, Long> {

    User getFirstByUsername(String username);

    /**
     * {@code :search} is cast explicitly, same as {@code TaskRepository.searchTasks} - left
     * untyped, a null value here makes Postgres's JDBC driver default the parameter to
     * {@code bytea}, which then blows up the moment it reaches {@code LOWER(...)}.
     */
    @Query("""
        SELECT u FROM User u
        WHERE CAST(:search AS string) IS NULL
          OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
        ORDER BY u.id
        LIMIT :limit OFFSET :offset
    """)
    List<User> searchUsers(String search, Integer offset, Integer limit);

    @Query("""
        SELECT COUNT(u) FROM User u
        WHERE CAST(:search AS string) IS NULL
          OR LOWER(u.username) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
    """)
    long countSearchUsers(String search);
}
