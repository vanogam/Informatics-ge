package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserProfileDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;

public interface IUserManager {

    /** Sentinel id for a request with nobody logged in - never a real user's id. */
    long ANONYMOUS_USER_ID = -1L;

    User getUser(Long userId);

    User getUserByUsername(String username);

    boolean isAdmin(Long userId);

    void createUser(UserDTO user, String password) throws InformaticsServerException;

    User authenticate(String username, String password) throws InformaticsServerException;

    void editUser(User user);

    boolean isLoggedIn() ;

    UserDTO getAuthenticatedUser() throws InformaticsServerException;

    /**
     * The current user's id, or {@link #ANONYMOUS_USER_ID} when nobody is logged in. Never
     * throws - for endpoints that must still work for an anonymous viewer of an open room.
     */
    long getAuthenticatedUserIdOrAnonymous();

    void addPasswordRecoveryQuery(String username) throws InformaticsServerException;

    RecoverPassword verifyRecoveryQuery(String link) throws InformaticsServerException;

    void recoverPassword(String link, String newPassword) throws InformaticsServerException;

    UserProfileDTO getUserProfile(Long userId) throws InformaticsServerException;

    UserProfileDTO getUserProfileByUsername(String username) throws InformaticsServerException;

    void changePassword(String oldPassword, String newPassword) throws InformaticsServerException;
}
