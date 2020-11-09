package su.nepom.cash.importaccess.job;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class JobConfig {
    private final JobBuilderFactory jobBuilderFactory;

    @Bean
    public Job importJob(Step deleteAllStep,
                         Step currencyStep,
                         Step accountStep,
                         Step recordStep,
                         Step closeAccountStep
    ) {
        return jobBuilderFactory.get("Import")
                .incrementer(new RunIdIncrementer())
                .flow(deleteAllStep)
                .next(currencyStep)
                .next(accountStep)
                .next(recordStep)
                .next(closeAccountStep)
                .end()
                .build();
    }
}
