package su.nepom.cash.importaccess.job;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import su.nepom.cash.importaccess.Split;
import su.nepom.cash.importaccess.domain.Account;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * В access были счета с одинаковыми именами и разными валютами (и даже просто счета с одинаковыми именами)
 * А в новой БД есть контроль уникальности имени счета.
 * <p>
 * Поэтому приходится читать все счета, обрабатывать дубликаты и далее писать в базу
 * <p>
 * Все счета сохраняются в SharedData для использования в других шагах
 */
@Configuration
@AllArgsConstructor
public class AccountStepConfig {
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step accountStep(FlatFileItemReader<Account> reader, AccountStep accountStep) {
        return stepBuilderFactory.get("accountStep")
                .tasklet(accountStep)
                .stream(reader)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<Account> reader(JobParams jobParams) {
        return new FlatFileItemReaderBuilder<Account>()
                .name("accountItemReader")
                .resource(new FileSystemResource(jobParams.getInputDir() + "/Счета.txt"))
                .lineMapper((s, i) -> {
                    var values = Split.splitAndConvert(s, "isiiiii");
                    return new Account()
                            .setId((Integer) values.get(0))
                            .setOriginalName((String) values.get(1))
                            .setCurrencyId((Integer) values.get(2))
                            .setClosed(values.get(3).equals(1))
                            .setMoney(values.get(4).equals(1));
                })
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Account> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Account>()
                .dataSource(dataSource)
                .sql("insert into account(id, name, closed, is_money, currency_id, total) values(:id, :name, :closed, :money, :currencyId, 0)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @StepScope
    @Component
    public static class AccountStep implements Tasklet {
        private final ItemReader<Account> reader;
        private final ItemWriter<Account> writer;
        private final List<Account> accounts = new ArrayList<>();
        private final SharedData sharedData;

        public AccountStep(FlatFileItemReader<Account> reader, JdbcBatchItemWriter<Account> writer, SharedData sharedData) {
            this.reader = reader;
            this.writer = writer;
            this.sharedData = sharedData;
        }

        @Override
        @SneakyThrows
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            readAllAndChangeName();
            writer.write(accounts);
            return RepeatStatus.FINISHED;
        }

        @SneakyThrows
        private void readAllAndChangeName() {
            for (var account = reader.read(); account != null; account = reader.read()) {
                changeNameIfNeed(account);
                accounts.add(account);
                sharedData.addAccount(account);
            }
        }

        private void changeNameIfNeed(Account account) {
            for (var otherAccount : accounts) {
                if (otherAccount.getOriginalName().equals(account.getOriginalName())) {
                    if (otherAccount.getCurrencyId() == account.getCurrencyId()) {
                        addIdToName(otherAccount);
                        addIdToName(account);
                    } else {
                        addCurrencyToName(otherAccount);
                        addCurrencyToName(account);
                    }
                    break;
                }
            }
        }

        private void addCurrencyToName(Account account) {
            if (account.getOriginalName().equals(account.getName())) {
                account.setName(account.getName() + ", " + sharedData.getCurrency(account.getCurrencyId()).getCode());
            }
        }

        private void addIdToName(Account account) {
            if (account.getOriginalName().equals(account.getName())) {
                account.setName(account.getName() + ", " + account.getId());
            }
        }
    }
}
