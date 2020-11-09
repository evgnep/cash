package su.nepom.cash.importaccess;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class BatchConfigurer extends DefaultBatchConfigurer {
    @Override
    public void setDataSource(DataSource dataSource) {
    }
}
