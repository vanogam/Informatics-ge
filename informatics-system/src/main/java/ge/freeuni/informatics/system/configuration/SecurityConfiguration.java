package ge.freeuni.informatics.system.configuration;

import ge.freeuni.informatics.common.model.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class SecurityConfiguration {

    @Configuration
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public static class MainSecurityAdapter extends WebSecurityConfigurerAdapter {
        private static final String[] GLOBAL_ADDRESSES = {"/",
                "/api/login",
                "/api/logout",
                "/api/register",
                "/api/contest-list"
        };

        private static final String[] ALL_ACCOUNT_ADDRESSES = {"/profile"};

        @Autowired
        private void configureGlobal(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(new InformaticsAuthenticationProvider());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .antMatchers(GLOBAL_ADDRESSES)
                    .permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .exceptionHandling()
                    .authenticationEntryPoint((request, response, authException) -> {
                        // Return 401 Unauthorized for unauthenticated requests
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        // Return 403 Forbidden for authenticated users without proper permissions
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                    })
                    .and().logout()
                    .invalidateHttpSession(true)
                    .and().csrf().disable();
        }
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Configuration
    public static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final String[] BASIC_AUTH_SERVICES = new String[]{
                "/cms-api/**"
        };

        @Autowired
        private void configureGlobal(AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(new InformaticsAuthenticationProvider());
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers()
                    .antMatchers(BASIC_AUTH_SERVICES)
                    .and()
                    .authorizeRequests().anyRequest().authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .csrf().disable();
        }
    }
}
