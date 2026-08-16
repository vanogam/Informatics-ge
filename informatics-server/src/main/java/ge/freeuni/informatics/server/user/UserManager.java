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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class UserManager implements IUserManager {

    private static final Logger log = LoggerFactory.getLogger(UserManager.class);



    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    /**
     * How stale the recorded login time has to be before it is written again. Anything shorter
     * turns every service-account heartbeat into a contended write for no useful precision.
     */
    private static final long LAST_LOGIN_WRITE_INTERVAL_MS = 60_000;

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
    /**
     * Verifies credentials and records the login.
     *
     * <p>User rows carry a {@code @Version}, so two authentications of the same account at the
     * same moment collide on the lastLogin write. Service accounts make that routine rather than
     * rare: every worker heartbeats over Basic auth every 30 seconds as the same user. The write
     * is therefore skipped when the recorded time is already fresh, and retried when it does
     * race; a lost race must never cost a valid login.
     */
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3, backoff = @Backoff(delay = 25, multiplier = 2))
    public User authenticate(String username, String password) {
        User user = userRepository.getFirstByUsername(username);
        if (user == null) {
            return null;
        }

        boolean migrated = false;
        boolean authenticated;
        if (isBcryptHash(user.getPassword())) {
            authenticated = BCRYPT.matches(password, user.getPassword());
        } else {
            String legacyHash = UserUtils.getHash(password, user.getPasswordSalt());
            authenticated = legacyHash.equals(user.getPassword());
            if (authenticated) {
                user.setPassword(BCRYPT.encode(password));
                user.setPasswordSalt("");
                migrated = true;
            }
        }

        if (!authenticated) {
            return null;
        }
        recordLogin(user, migrated);
        return user;
    }

    /**
     * Last resort once the retries are used up. The lastLogin write is bookkeeping and must not
     * turn a valid credential into a failed login.
     *
     * <p>Credentials are checked again here rather than assuming the failed attempt got far
     * enough to verify them, so this can never hand back an unauthenticated user.
     */
    @Recover
    public User recoverFromLockContention(ObjectOptimisticLockingFailureException e,
                                          String username, String password) {
        log.warn("Could not record login for {} because of concurrent updates; "
                + "authentication itself is unaffected", username);
        User user = userRepository.getFirstByUsername(username);
        if (user == null) {
            return null;
        }
        if (isBcryptHash(user.getPassword())) {
            return BCRYPT.matches(password, user.getPassword()) ? user : null;
        }
        return UserUtils.getHash(password, user.getPasswordSalt()).equals(user.getPassword())
                ? user : null;
    }

    /**
     * Persists the login, skipping the write when the stored timestamp is recent enough. A
     * password just migrated to bcrypt always writes, since dropping it would leave the account
     * on its legacy hash.
     */
    private void recordLogin(User user, boolean migrated) {
        Date now = new Date();
        boolean stale = user.getLastLogin() == null
                || now.getTime() - user.getLastLogin().getTime() > LAST_LOGIN_WRITE_INTERVAL_MS;
        if (!migrated && !stale) {
            return;
        }
        user.setLastLogin(now);
        userRepository.save(user);
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
