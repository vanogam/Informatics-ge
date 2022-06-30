package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.exception.InformaticsServerException;

public interface IUserManager {

    void createUser(UserDTO userDTO);

    UserDTO authenticate(String username, String password) throws InformaticsServerException;

    void editUser(UserDTO userDTO);

    UserDTO getAuthenticatedUser() throws InformaticsServerException;

}
