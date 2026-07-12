package ge.freeuni.informatics.system.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ApiConfiguration {

    @Value("${ge.freeuni.informatics.cors.allowedOrigins:}")
    private String allowedOrigins;

    /** Comma-separated Ant-style patterns (e.g. http://192.168.*:*). Preferred for LAN dev access. */
    @Value("${ge.freeuni.informatics.cors.allowedOriginPatterns:}")
    private String allowedOriginPatterns;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        if (allowedOriginPatterns != null && !allowedOriginPatterns.isBlank()) {
            config.setAllowedOriginPatterns(splitCsv(allowedOriginPatterns));
        } else if (allowedOrigins == null || allowedOrigins.isBlank()) {
            config.setAllowedOriginPatterns(List.of("https://*"));
        } else {
            config.setAllowedOrigins(splitCsv(allowedOrigins));
        }

        config.setAllowedHeaders(
                List.of("Content-Type", "X-XSRF-TOKEN", "Authorization", "Accept", "Origin", "X-Requested-With"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Register CORS for all paths so OPTIONS preflight matches even if the client targets a non-/api URL.
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static List<String> splitCsv(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
