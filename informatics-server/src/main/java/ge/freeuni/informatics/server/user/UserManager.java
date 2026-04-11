package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserProfileDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.model.user.UserRole;
import ge.freeuni.informatics.common.security.InformaticsPrincipal;
import ge.freeuni.informatics.repository.user.PasswordRecoveryJpaRepository;
import ge.freeuni.informatics.repository.user.SolvedProblemJpaRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.utils.FileUtils;
import ge.freeuni.informatics.utils.MailSender;
import ge.freeuni.informatics.utils.UserUtils;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class UserManager implements IUserManager {

    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    @Value("${ge.freeuni.informatics.server.user.passwordRecoveryValidityMinutes}")
    private String passwordRecoveryValidityMinutes;

    @Value("${ge.freeuni.informatics.mail.address}")
    private String emailAddress;

    @Value("${ge.freeuni.informatics.mail.password}")
    private String emailPassword;

    @Value("${ge.freeuni.informatics.mail.host}")
    private String emailHost;

    @Value("${ge.freeuni.informatics.host}")
    private String host;

    @Value("${server.port.front}")
    private String port;

    final UserJpaRepository userRepository;

    final PasswordRecoveryJpaRepository recoveryJpaRepository;

    final MailSender mailSender;

    final SolvedProblemJpaRepository solvedProblemRepository;

    @Autowired
    public UserManager(UserJpaRepository userRepository,
                       PasswordRecoveryJpaRepository passwordRecoveryJpaRepository,
                       MailSender mailSender,
                       SolvedProblemJpaRepository solvedProblemRepository) {
        this.userRepository = userRepository;
        this.recoveryJpaRepository = passwordRecoveryJpaRepository;
        this.mailSender = mailSender;
        this.solvedProblemRepository = solvedProblemRepository;
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.getFirstByUsername(username);
    }

    @Override
    public void createUser(UserDTO userDTO, String password) throws InformaticsServerException {
        User user = UserDTO.fromDTO(userDTO);
        user.setId(null);
        user.setPasswordSalt("");
        user.setPassword(BCRYPT.encode(password));
        user.setVersion(1);
        user.setRole(UserRole.STUDENT.name());
        user.setRegistrationTime(new Date());
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw InformaticsServerException.USERNAME_ALREADY_EXISTS;
        }
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userRepository.getFirstByUsername(username);
        if (user == null) {
            return null;
        }

        boolean authenticated;
        if (isBcryptHash(user.getPassword())) {
            authenticated = BCRYPT.matches(password, user.getPassword());
        } else {
            String legacyHash = UserUtils.getHash(password, user.getPasswordSalt());
            authenticated = legacyHash.equals(user.getPassword());
            if (authenticated) {
                user.setPassword(BCRYPT.encode(password));
                user.setPasswordSalt("");
            }
        }

        if (authenticated) {
            user.setLastLogin(new Date());
            userRepository.save(user);
            return user;
        }

        return null;
    }

    private static boolean isBcryptHash(String hash) {
        return hash != null && hash.startsWith("$2");
    }

    @Override
    public void editUser(User user) {

    }

    @Override
    public boolean isLoggedIn() {
        Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principalObject instanceof InformaticsPrincipal;
    }

    @Override
    public UserDTO getAuthenticatedUser() throws InformaticsServerException {
        Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principalObject instanceof InformaticsPrincipal principal)) {
            throw InformaticsServerException.NOT_LOGGED_IN;
        }
        return UserDTO.toDTO(principal.getUser());
    }

    @Override
    public void addPasswordRecoveryQuery(String username) throws InformaticsServerException {
        RecoverPassword recoverPassword = new RecoverPassword();
        User user;
        try {
            user = userRepository.getFirstByUsername(username);
            recoverPassword.setUserId(user.getId());
        } catch (NoResultException ex) {
            throw InformaticsServerException.INVALID_USERNAME;
        }
        recoverPassword.setCreateTime(new Date());
        recoverPassword.setLink(FileUtils.getRandomFileName(30));
        recoverPassword.setUsed(false);
        mailSender.sendMail(emailAddress,
                user.getEmail(),
                emailPassword,
                emailHost,
                generateRecoverText(recoverPassword.getLink()),
                getRecoverSubject());
        recoveryJpaRepository.save(recoverPassword);
    }

    @Override
    public RecoverPassword verifyRecoveryQuery(String link) throws InformaticsServerException {
        RecoverPassword recoverPassword;
        try {
            recoverPassword = recoveryJpaRepository.getFirstByLink(link);
        } catch (NoResultException ex) {
            throw InformaticsServerException.INVALID_RECOVERY_LINK;
        }
        if (recoverPassword.isUsed()) {
            throw InformaticsServerException.RECOVERY_LINK_ALREADY_USED;
        }
        Date createTime = recoverPassword.getCreateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createTime);
        calendar.add(Calendar.MINUTE, Integer.parseInt(passwordRecoveryValidityMinutes));
        if (calendar.getTime().before(new Date())) {
            throw InformaticsServerException.RECOVERY_REQUEST_TOO_OLD;
        }
        return recoverPassword;
    }

    @Override
    public void recoverPassword(String link, String newPassword) throws InformaticsServerException {
        RecoverPassword recoverPassword = verifyRecoveryQuery(link);
        User user = userRepository.getReferenceById(recoverPassword.getUserId());
        user.setPasswordSalt("");
        user.setPassword(BCRYPT.encode(newPassword));
        userRepository.save(user);
        recoverPassword.setUsed(true);
        recoveryJpaRepository.save(recoverPassword);
    }

    private String getRecoverSubject() {
        return "პაროლის აღდგენა";
    }

    private String generateRecoverText(String link) {
        String address = host + ("80".equals(port) ? "" : ":" + port) + "/recover/update-password/" + link;
        return "პაროლის აღსადგენად გადადით მოცემულ ლინკზე\n" + address;
    }

    @Override
    public UserProfileDTO getUserProfile(Long userId) throws InformaticsServerException {
        User user = userRepository.getReferenceById(userId);
        long solvedProblemsCount = solvedProblemRepository.countByUserIdAndStatus(userId, ProblemAttemptStatus.SOLVED);
        return new UserProfileDTO(
                user.getUsername(),
                solvedProblemsCount,
                user.getLastLogin(),
                user.getRegistrationTime()
        );
    }

    @Override
    public UserProfileDTO getUserProfileByUsername(String username) throws InformaticsServerException {
        User user = userRepository.getFirstByUsername(username);
        if (user == null) {
            throw InformaticsServerException.USER_NOT_FOUND;
        }
        long solvedProblemsCount = solvedProblemRepository.countByUserIdAndStatus(user.getId(), ProblemAttemptStatus.SOLVED);
        return new UserProfileDTO(
                user.getUsername(),
                solvedProblemsCount,
                user.getLastLogin(),
                user.getRegistrationTime()
        );
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) throws InformaticsServerException {
        UserDTO currentUser = getAuthenticatedUser();
        User user = userRepository.getFirstByUsername(currentUser.username());
        
        if (user == null) {
            throw InformaticsServerException.USER_NOT_FOUND;
        }
        
        boolean oldPasswordValid;
        if (isBcryptHash(user.getPassword())) {
            oldPasswordValid = BCRYPT.matches(oldPassword, user.getPassword());
        } else {
            String oldHash = UserUtils.getHash(oldPassword, user.getPasswordSalt());
            oldPasswordValid = oldHash.equals(user.getPassword());
        }
        if (!oldPasswordValid) {
            throw InformaticsServerException.INCORRECT_PASSWORD;
        }
        
        user.setPasswordSalt("");
        user.setPassword(BCRYPT.encode(newPassword));
        userRepository.save(user);
    }
}
