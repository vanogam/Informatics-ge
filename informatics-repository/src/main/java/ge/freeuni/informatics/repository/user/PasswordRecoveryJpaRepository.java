package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.common.model.user.RecoverPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRecoveryJpaRepository extends JpaRepository<RecoverPassword, Long> {
    RecoverPassword getFirstByLink(String link);
}
