package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.model.user.UserRole;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.security.InformaticsPrincipal;
import ge.freeuni.informatics.repository.user.IUserRepository;
import ge.freeuni.informatics.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public User getUser(Long userId) {
        return userRepository.getUser(userId);
    }

    @Override
    public void createUser(User user) {
        user.setPasswordSalt(UserUtils.getSalt());
        user.setPassword(UserUtils.getHash(user.getPassword(), user.getPasswordSalt()));
        user.setVersion(1);
        addRole(user, UserRole.STUDENT);
        userRepository.addUser(user);

    }

    @Override
    public User authenticate(String username, String password) throws InformaticsServerException {
        try {
            User user = userRepository.getUser(username);
            String hash = UserUtils.getHash(password, user.getPasswordSalt());
            if (hash.equals(user.getPassword())) {
                return user;
            }
        } catch (NoResultException ignored) {
            return null;
        }
        return null;
    }

    @Override
    public void editUser(User user) {

    }

    @Override
    public UserDTO getAuthenticatedUser() throws InformaticsServerException {
        Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principalObject instanceof InformaticsPrincipal)) {
            throw new InformaticsServerException("Not logged in");
        }
        InformaticsPrincipal principal = (InformaticsPrincipal) principalObject;
        return UserDTO.toDTO(principal.getUser());

    }

    private void addRole(User user, UserRole role) {
        if (user.getRoles() == null) {
            user.setRoles("");
        }
        if (user.getRoles().contains(role.name())) {
            return;
        }
        String delimiter = ",";
        if (user.getRoles().isEmpty()) {
            delimiter = "";
        }
        user.setRoles(user.getRoles() + delimiter + role.name());
    }
}
