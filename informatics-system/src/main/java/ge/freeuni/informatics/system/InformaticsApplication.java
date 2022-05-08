package ge.freeuni.informatics.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ge.freeuni.informatics")
public class InformaticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(InformaticsApplication.class, args);
    }

}
