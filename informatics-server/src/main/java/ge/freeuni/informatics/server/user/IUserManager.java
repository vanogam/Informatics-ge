package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.model.dto.AuthenticationDetails;
import ge.freeuni.informatics.model.dto.UserDTO;

public interface IUserManager {

    void createUser(UserDTO userDTO);

    UserDTO authenticate(AuthenticationDetails auth);

    void editUser(UserDTO userDTO);

}
