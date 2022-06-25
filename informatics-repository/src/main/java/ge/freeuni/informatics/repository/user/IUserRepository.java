package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.model.entity.user.User;
import ge.freeuni.informatics.model.exception.InformaticsServerException;

public interface IUserRepository {

    User getUser(String username) throws InformaticsServerException;

    void addUser(User user);
}
