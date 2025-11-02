package ge.freeuni.informatics.system.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class AccessDataSourceConfig {

    @Bean(name = "accessDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.access")
    public DataSource accessDataSource() {
        return new DriverManagerDataSource();
    }
}
