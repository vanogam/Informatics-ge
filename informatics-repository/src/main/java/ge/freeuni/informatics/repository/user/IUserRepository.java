package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

public interface IUserRepository {

    User getUser(Long id);

    User getUser(String username) throws InformaticsServerException;

    void addUser(User user);
}
