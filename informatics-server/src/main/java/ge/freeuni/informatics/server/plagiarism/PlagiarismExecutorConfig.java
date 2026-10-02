package ge.freeuni.informatics.server.plagiarism;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A small, dedicated pool for running JPlag checks off the request thread. Kept separate from
 * request handling and from judge integration - a plagiarism check is CPU-heavy and admin
 * triggered, and must not compete with either.
 */
@Configuration
public class PlagiarismExecutorConfig {

    @Bean
    public ExecutorService plagiarismExecutor() {
        return Executors.newFixedThreadPool(2);
    }
}
