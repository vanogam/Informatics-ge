package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserProfileDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;

public interface IUserManager {

    User getUser(Long userId);

    User getUserByUsername(String username);

    void createUser(UserDTO user, String password) throws InformaticsServerException;

    User authenticate(String username, String password) throws InformaticsServerException;

    void editUser(User user);

    boolean isLoggedIn() ;

    UserDTO getAuthenticatedUser() throws InformaticsServerException;

    void addPasswordRecoveryQuery(String username) throws InformaticsServerException;

    RecoverPassword verifyRecoveryQuery(String link) throws InformaticsServerException;

    void recoverPassword(String link, String newPassword) throws InformaticsServerException;

    UserProfileDTO getUserProfile(Long userId) throws InformaticsServerException;

    UserProfileDTO getUserProfileByUsername(String username) throws InformaticsServerException;

    void changePassword(String oldPassword, String newPassword) throws InformaticsServerException;
}
