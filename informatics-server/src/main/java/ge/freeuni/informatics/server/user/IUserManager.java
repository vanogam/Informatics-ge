package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

public interface IUserManager {

    User getUser(Long userId);

    void createUser(UserDTO user, String password) throws InformaticsServerException;

    User authenticate(String username, String password) throws InformaticsServerException;

    void editUser(User user);

    boolean isLoggedIn() ;

    UserDTO getAuthenticatedUser() throws InformaticsServerException;

    void addPasswordRecoveryQuery(String username) throws InformaticsServerException;

    RecoverPassword verifyRecoveryQuery(String link) throws InformaticsServerException;

    void recoverPassword(String link, String newPassword) throws InformaticsServerException;
}
