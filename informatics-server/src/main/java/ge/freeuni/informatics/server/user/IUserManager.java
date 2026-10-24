package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.AdminUserDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserProfileDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;

import java.util.List;

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

    /** Updates the authenticated user's email/first/last name. The username cannot be changed here. */
    void updateProfile(String email, String firstName, String lastName) throws InformaticsServerException;

    /** Every user matching {@code search} against username/first/last name, for the admin users page. */
    List<AdminUserDTO> listUsers(String search, int offset, int limit);

    /** The total count {@link #listUsers} would page through for this search, ignoring offset/limit. */
    long countUsers(String search);

    /**
     * Disables or re-enables a user's account. A disabled user can no longer log in, but nothing
     * about their existing submissions, posts or standings is touched - see {@link #authenticate}.
     * Refuses to disable an admin account, so an admin can never be locked out by mistake.
     */
    void setUserDisabled(Long userId, boolean disabled) throws InformaticsServerException;
}
