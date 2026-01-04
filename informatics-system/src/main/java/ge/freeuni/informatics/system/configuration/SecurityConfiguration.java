package ge.freeuni.informatics.system.configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
    private static final String[] GLOBAL_ADDRESSES = {"/",
            "/api/login",
            "/api/room/1/posts",
            "/api/logout",
            "/api/register",
            "/api/contests",
            "/api/csrf",
            "/error",
    };

    private static final String[] ALL_ACCOUNT_ADDRESSES = {"/profile"};

    @Value("${ge.freeuni.informatics.security.rememberMe.key}")
    private String rememberMeKey;

    @Value("${ge.freeuni.informatics.security.rememberMe.tokenValiditySeconds}")
    private int rememberMeTokenValiditySeconds;

    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfiguration(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    private void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new InformaticsAuthenticationProvider());
    }

    @Bean
    @Order(1)
    public SecurityFilterChain workerHeartbeatSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/admin/workers/**/heartbeat")
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().hasRole("WORKER")
                )
                .httpBasic(httpBasic -> httpBasic
                        .realmName("Informatics Worker API")
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                );
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
        delegate.setCsrfRequestAttributeName("_csrf");
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
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeServices(rememberMeServices())
                        .key(rememberMeKey)
                        .tokenValiditySeconds(rememberMeTokenValiditySeconds)
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .deleteCookies("remember-me", "JSESSIONID")
                );
        return http.build();
    }

    @Bean
    public TokenBasedRememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices rememberMeServices = 
                new TokenBasedRememberMeServices(rememberMeKey, userDetailsService);
        rememberMeServices.setTokenValiditySeconds(rememberMeTokenValiditySeconds);
        rememberMeServices.setCookieName("remember-me");
        rememberMeServices.setAlwaysRemember(false);
        return rememberMeServices;
    }
}
