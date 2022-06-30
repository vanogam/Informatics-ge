package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.model.entity.user.User;
import ge.freeuni.informatics.model.exception.InformaticsServerException;
import org.apache.catalina.LifecycleState;
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
}
