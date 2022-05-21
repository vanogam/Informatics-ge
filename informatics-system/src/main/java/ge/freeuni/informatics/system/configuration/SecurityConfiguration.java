package ge.freeuni.informatics.system.configuration;

import ge.freeuni.informatics.model.UserRole;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static final String[] GLOBAL_ADDRESSES = {"/",
                                                      "/login",
                                                      "/register"};

    private static final String[] ALL_ACCOUNT_ADDRESSES = {"/logout", "/profile"};



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .antMatchers(GLOBAL_ADDRESSES)
                .permitAll()
                .antMatchers(ALL_ACCOUNT_ADDRESSES)
                .hasAnyAuthority(UserRole.ADMIN.name(), UserRole.TEACHER.name(), UserRole.STUDENT.name())
                .and().csrf().disable();
    }
}
