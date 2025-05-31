package ge.freeuni.informatics.system.configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers(GLOBAL_ADDRESSES).permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) ->
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                    )
                    .accessDeniedHandler((request, response, accessDeniedException) ->
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                    )
            )
            .logout(logout -> logout
                    .invalidateHttpSession(true)
            );
        return http.build();
    }
}
