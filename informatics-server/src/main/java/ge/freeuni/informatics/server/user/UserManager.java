package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.model.user.UserRole;
import ge.freeuni.informatics.common.security.InformaticsPrincipal;
import ge.freeuni.informatics.repository.user.PasswordRecoveryJpaRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.utils.FileUtils;
import ge.freeuni.informatics.utils.MailSender;
import ge.freeuni.informatics.utils.UserUtils;
import jakarta.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class UserManager implements IUserManager {

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

    @Autowired
    public UserManager(UserJpaRepository userRepository,
                       PasswordRecoveryJpaRepository passwordRecoveryJpaRepository,
                       MailSender mailSender) {
        this.userRepository = userRepository;
        this.recoveryJpaRepository = passwordRecoveryJpaRepository;
        this.mailSender = mailSender;
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.getReferenceById(userId);
    }

    @Override
    public void createUser(UserDTO userDTO, String password) throws InformaticsServerException {
        User user = UserDTO.fromDTO(userDTO);
        user.setId(null);
        user.setPasswordSalt(UserUtils.getSalt());
        user.setPassword(UserUtils.getHash(password, user.getPasswordSalt()));
        user.setVersion(1);
        user.setRole(UserRole.STUDENT.name());
        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new InformaticsServerException("usernameAlreadyExists");
        }
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userRepository.getFirstByUsername(username);
        if (user == null) {
            return null;
        }
        String hash = UserUtils.getHash(password, user.getPasswordSalt());
        if (hash.equals(user.getPassword())) {
            return user;
        }

        return null;
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
            throw new InformaticsServerException("notLoggedIn");
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
            throw new InformaticsServerException("invalidUsername");
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
            throw new InformaticsServerException("invalidLink");
        }
        if (recoverPassword.isUsed()) {
            throw new InformaticsServerException("linkAlreadyUsed");
        }
        Date createTime = recoverPassword.getCreateTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createTime);
        calendar.add(Calendar.MINUTE, Integer.parseInt(passwordRecoveryValidityMinutes));
        if (calendar.getTime().before(new Date())) {
            throw new InformaticsServerException("recoveryRequestTooOld");
        }
        return recoverPassword;
    }

    @Override
    public void recoverPassword(String link, String newPassword) throws InformaticsServerException {
        RecoverPassword recoverPassword = verifyRecoveryQuery(link);
        User user = userRepository.getReferenceById(recoverPassword.getUserId());
        user.setPasswordSalt(UserUtils.getSalt());
        user.setPassword(UserUtils.getHash(newPassword, user.getPasswordSalt()));
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
}
