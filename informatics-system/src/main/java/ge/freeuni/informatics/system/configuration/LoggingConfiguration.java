package ge.freeuni.informatics.system.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class LoggingConfiguration {

    private static final String HEARTBEAT_PATH_REGEX = "/api/admin/workers/[^/]+/heartbeat";

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter() {
            @Override
            protected boolean shouldLog(HttpServletRequest request) {
                // Heartbeats fire every few seconds per worker and are already logged, with more
                // useful detail (status transitions, jobs processed), by WorkerManager itself.
                if (request.getRequestURI().matches(HEARTBEAT_PATH_REGEX)) {
                    return false;
                }
                return super.shouldLog(request);
            }
        };
        filter.setIncludeQueryString(true);
        // Never log raw bodies: login/register/changed-password JSON contains secrets.
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("Inbound request: ");
        return filter;
    }
}
