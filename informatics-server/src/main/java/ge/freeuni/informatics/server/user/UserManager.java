package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.model.dto.AuthenticationDetails;
import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.entity.User;
import ge.freeuni.informatics.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class UserManager implements IUserManager{

    @Override
    public void createUser(UserDTO userDTO) {
        User user = UserDTO.fromDTO(userDTO);
        user.setPasswordSalt(UserUtils.getSalt());
        user.setPassword(UserUtils.getHash(user.getPassword(), user.getPasswordSalt()));

    }

    @Override
    public UserDTO authenticate(AuthenticationDetails auth) {
        return null;
    }

    @Override
    public void editUser(UserDTO userDTO) {

    }
}
