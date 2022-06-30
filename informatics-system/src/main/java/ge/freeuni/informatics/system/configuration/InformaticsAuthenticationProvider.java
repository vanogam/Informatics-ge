package ge.freeuni.informatics.system.configuration;

import ge.freeuni.informatics.model.dto.AuthenticationDetails;
import ge.freeuni.informatics.model.dto.UserDTO;
import ge.freeuni.informatics.model.exception.InformaticsServerException;
import ge.freeuni.informatics.model.security.InformaticsPrincipal;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.BeanUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
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
        try {
            UserDTO user = userManager.authenticate(username, password);
            if (user == null) {
                throw new AuthenticationServiceException("Invalid login");
            }
            return new UsernamePasswordAuthenticationToken(new InformaticsPrincipal(user), null, getRoles(user.getRoles()));
        } catch (InformaticsServerException e) {
            throw new RuntimeException(e);
        }


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
            roles.add(new SimpleGrantedAuthority(role));
        }
        return roles;
    }
}
