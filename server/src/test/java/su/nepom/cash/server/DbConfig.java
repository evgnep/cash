package su.nepom.cash.server;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.opentable.db.postgres.embedded.LiquibasePreparer;
import com.opentable.db.postgres.junit5.EmbeddedPostgresExtension;
import com.opentable.db.postgres.junit5.SingleInstancePostgresExtension;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@Profile("test")
public class DbConfig {
    @Bean
    public DataSource dataSource() throws Exception {
        var pg = EmbeddedPostgres.builder().start();
        return pg.getPostgresDatabase();
    }
}
