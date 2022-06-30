package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.entity.user.User;
import ge.freeuni.informatics.model.exception.InformaticsServerException;
import ge.freeuni.informatics.model.security.InformaticsPrincipal;
import ge.freeuni.informatics.repository.user.IUserRepository;
import ge.freeuni.informatics.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
import java.security.Principal;

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
    public UserDTO authenticate(String username, String password) throws InformaticsServerException {
        try {
            User user = userRepository.getUser(username);
            String hash = UserUtils.getHash(password, user.getPasswordSalt());
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

    @Override
    public UserDTO getAuthenticatedUser() throws InformaticsServerException {
        Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null) {
            throw new InformaticsServerException("Not logged in");
        }
        if (principal instanceof InformaticsPrincipal) {
            return ((InformaticsPrincipal) principal).getUser();
        }

        throw new InformaticsServerException("Unexpected exception");

    }
}
