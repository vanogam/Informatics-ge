package ge.freeuni.informatics.system.configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
    private static final String[] GLOBAL_ADDRESSES = {"/",
            "/api/login",
            "/api/logout",
            "/api/register",
            "/api/contests",
            "/api/csrf",
            "/error",
    };

    private static final String[] ALL_ACCOUNT_ADDRESSES = {"/profile"};

    @Autowired
    private void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new InformaticsAuthenticationProvider());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
        // set the name of the attribute the CsrfToken will be populated on
        delegate.setCsrfRequestAttributeName("_csrf");
        // Use only the handle() method of XorCsrfTokenRequestAttributeHandler and the
        // default implementation of resolveCsrfTokenValue() from CsrfTokenRequestHandler
        CsrfTokenRequestHandler requestHandler = delegate::handle;
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(GLOBAL_ADDRESSES).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                                    System.out.println("Unauthorized access attempt: " + request.getRequestURI());
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                }
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    System.out.println("Access denied: " + request.getRequestURI());
                                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                                }
                        )
                )
                .csrf((csrf) -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(requestHandler)
                        .ignoringRequestMatchers("/api/csrf")
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                );
        return http.build();
    }
}
