package ge.freeuni.informatics.repository.user;

import ge.freeuni.informatics.model.entity.user.User;

public interface IUserRepository {

    User getUser(String username);

    void addUser(User user);
}
