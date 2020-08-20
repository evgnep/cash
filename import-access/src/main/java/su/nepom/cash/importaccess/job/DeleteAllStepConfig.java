package su.nepom.cash.importaccess.job;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * Удаляет текущие данные из всех таблиц, куда будут импортироваться данные из access
 */
@Configuration
@AllArgsConstructor
public class DeleteAllStepConfig {
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step deleteAllStep(JdbcOperations operations) {
        return stepBuilderFactory.get("deleteAllStep")
                .tasklet(new DeleteAllStep(operations))
                .build();
    }

    @AllArgsConstructor
    private static class DeleteAllStep implements Tasklet {
        private final JdbcOperations operations;

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            operations.update("delete from record");
            operations.update("delete from account");
            operations.update("delete from currency");

            return RepeatStatus.FINISHED;
        }
    }
}
