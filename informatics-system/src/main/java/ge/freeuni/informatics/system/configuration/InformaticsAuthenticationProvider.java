package ge.freeuni.informatics.system.configuration;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.security.InformaticsPrincipal;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.BeanUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;

public class InformaticsAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        IUserManager userManager = getUserManager();
        User user;
        try {
            user = userManager.authenticate(username, password);
        } catch (InformaticsServerException e) {
            throw new RuntimeException(e);
        }
        if (user == null) {
            throw new AuthenticationServiceException("Invalid login");
        }
        if (user.isDisabled()) {
            // DisabledException is an AuthenticationException, so unlike the RuntimeException
            // above it survives request.login()'s rethrow as a ServletException instead of
            // surfacing as an unhandled 500 - the one reason that needs to reach the login
            // response as a distinct code, rather than folding into "incorrectCredentials".
            throw new DisabledException(InformaticsServerException.ACCOUNT_DISABLED.getCode());
        }
        return new UsernamePasswordAuthenticationToken(new InformaticsPrincipal(user), null, getRoles(user.getRole()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }

    private IUserManager getUserManager() {
        return BeanUtils.getBean(IUserManager.class);
    }

    private List<GrantedAuthority> getRoles(String rolesString) {
        List<GrantedAuthority> roles = new ArrayList<>();
        if (rolesString == null) {
            return roles;
        }
        for (String role : rolesString.split(",")) {
            String trimmed = role.trim();
            if (!trimmed.isEmpty()) {
                roles.add(new SimpleGrantedAuthority("ROLE_" + trimmed));
            }
        }
        return roles;
    }
}
