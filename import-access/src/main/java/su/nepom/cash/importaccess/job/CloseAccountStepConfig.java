package su.nepom.cash.importaccess.job;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

/**
 * При импорте проводок закрытые счета были открыты тригером в БД
 * Заново их закроем
 */
@Configuration
@AllArgsConstructor
public class CloseAccountStepConfig {
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step closeAccountStep(CloseAccountStep closeAccountStep) {
        return stepBuilderFactory.get("closeAimpccountStep")
                .tasklet(closeAccountStep)
                .build();
    }


    @StepScope
    @Component
    @AllArgsConstructor
    public static class CloseAccountStep implements Tasklet {
        private final SharedData sharedData;
        private final JdbcOperations operations;

        @Override
        @SneakyThrows
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            for (var account : sharedData.getAccounts())
                if (account.isClosed())
                    operations.update("update account set closed = true where id = ?", account.getId());
            return RepeatStatus.FINISHED;
        }
    }
}
