package ge.freeuni.informatics.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "ge.freeuni.informatics")
@EnableJpaRepositories(basePackages = "ge.freeuni.informatics.repository")
@EnableScheduling
@EnableRetry
public class InformaticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InformaticsApplication.class, args);
    }

}
