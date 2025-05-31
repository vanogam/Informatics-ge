package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.common.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserJpaRepository extends JpaRepository<User, Long> {

    User getFirstByUsername(String username);
}
