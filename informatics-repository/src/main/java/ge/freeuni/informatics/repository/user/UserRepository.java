package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class UserRepository implements IUserRepository{

    @PersistenceContext
    EntityManager em;

    @Override
    public User getUser(Long id) {
        return em.find(User.class, id);
    }

    @Override
    public User getUser(String username) throws InformaticsServerException {
        String sql = "SELECT u FROM User u WHERE u.username = :username";
        TypedQuery<User> query = em.createQuery(sql, User.class)
                .setParameter("username", username);
        List<User> users = query.getResultList();
        if (users.size() == 0) {
            throw new InformaticsServerException("Invalid username/password");
        }
        return query.getSingleResult();
    }

    @Override
    public void addUser(User user) {
        em.persist(user);
    }

    @Override
    public void addPasswordRecoveryQuery(RecoverPassword query) {
        em.merge(query);
    }

    @Override
    public RecoverPassword getPasswordRecoveryQuery(String link) {
        String query = "SELECT r FROM RecoverPassword r WHERE r.link = :link";
        return em.createQuery(query, RecoverPassword.class)
                .setParameter("link", link)
                .getSingleResult();
    }
}
