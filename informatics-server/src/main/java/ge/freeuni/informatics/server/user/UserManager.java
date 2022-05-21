package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.model.dto.AuthenticationDetails;
import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.entity.User;
import ge.freeuni.informatics.repository.user.IUserRepository;
import ge.freeuni.informatics.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;

@Component
public class UserManager implements IUserManager{

    final IUserRepository userRepository;

    @Autowired
    public UserManager(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(UserDTO userDTO) {
        User user = UserDTO.fromDTO(userDTO);
        user.setPasswordSalt(UserUtils.getSalt());
        user.setPassword(UserUtils.getHash(user.getPassword(), user.getPasswordSalt()));

        userRepository.addUser(user);
    }

    @Override
    public UserDTO authenticate(AuthenticationDetails auth) {
        try {
            User user = userRepository.getUser(auth.getUsername());
            String hash = UserUtils.getHash(auth.getPassword(), user.getPasswordSalt());
            if (hash.equals(user.getPassword())) {
                return UserDTO.toDTO(user);
            }
        } catch (NoResultException ignored) {
            return null;
        }
        return null;
    }

    @Override
    public void editUser(UserDTO userDTO) {

    }
}
