package ge.freeuni.informatics.system.configuration;

import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.security.InformaticsPrincipal;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InformaticsUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userRepository;

    @Autowired
    public InformaticsUserDetailsService(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getFirstByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<GrantedAuthority> authorities = getRoles(user.getRole());
        InformaticsPrincipal principal = new InformaticsPrincipal(user);
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private List<GrantedAuthority> getRoles(String rolesString) {
        List<GrantedAuthority> roles = new ArrayList<>();
        if (rolesString == null) {
            return roles;
        }
        for (String role : rolesString.split(",")) {
            roles.add(new SimpleGrantedAuthority("ROLE_" + role));
        }
        return roles;
    }
}



