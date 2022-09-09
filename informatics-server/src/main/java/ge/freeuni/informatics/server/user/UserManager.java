package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.model.user.RecoverPassword;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.model.user.UserRole;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.security.InformaticsPrincipal;
import ge.freeuni.informatics.repository.user.IUserRepository;
import ge.freeuni.informatics.utils.FileUtils;
import ge.freeuni.informatics.utils.MailSender;
import ge.freeuni.informatics.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.NoResultException;
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
            throw new InformaticsServerException("notLoggedIn");
        }
        InformaticsPrincipal principal = (InformaticsPrincipal) principalObject;
        return UserDTO.toDTO(principal.getUser());

    }

    @Override
    public void addPasswordRecoveryQuery(String username) throws InformaticsServerException {
        RecoverPassword recoverPassword = new RecoverPassword();
        User user = userRepository.getUser(username);
        try {
            recoverPassword.setUserId(user.getId());
        } catch (NoResultException ex) {
            throw new InformaticsServerException("invalidUsername");
        }
        recoverPassword.setCreateTime(new Date());
        recoverPassword.setLink(FileUtils.getRandomFileName(30));
        recoverPassword.setUsed(false);
        MailSender.sendMail(emailAddress,
                user.getEmail(),
                emailPassword,
                emailHost,
                generateRecoverText(recoverPassword.getLink()),
                getRecoverSubject());
        userRepository.addPasswordRecoveryQuery(recoverPassword);

    }

    @Override
    public RecoverPassword verifyRecoveryQuery(String link) throws InformaticsServerException {
        RecoverPassword recoverPassword;
        try {
            recoverPassword = userRepository.getPasswordRecoveryQuery(link);
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
        User user = userRepository.getUser(recoverPassword.getUserId());
        user.setPasswordSalt(UserUtils.getSalt());
        user.setPassword(UserUtils.getHash(newPassword, user.getPasswordSalt()));
        userRepository.addUser(user);
        recoverPassword.setUsed(true);
        userRepository.addPasswordRecoveryQuery(recoverPassword);
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

    private String getRecoverSubject() {
        return "პაროლის აღდგენა";
    }

    private String generateRecoverText(String link) {
        String address = host + ("80".equals(port) ? "" : ":" + port) + "/recover/update-password/" + link;
        return "პაროლის აღსადგენად გადადით მოცემულ ლინკზე\n" + address;
    }
}
