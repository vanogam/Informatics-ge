package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.model.dto.AuthenticationDetails;
import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.exception.InformaticsServerException;

public interface IUserManager {

    void createUser(UserDTO userDTO);

    UserDTO authenticate(AuthenticationDetails auth) throws InformaticsServerException;

    void editUser(UserDTO userDTO);

}
