package su.nepom.cash.importaccess.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import su.nepom.cash.importaccess.Split;
import su.nepom.cash.importaccess.domain.Account;
import su.nepom.cash.importaccess.domain.Record;
import su.nepom.util.BigDecimals;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * В access проводки были организованы принципиально по другому, поэтому достаточно сложная логика преобразования
 * Так как запись идет в несколько таблиц, использовать стандартный Writer не вышло - сделал собственный,
 * заодно в него же вставил логику преобразования
 */
@Configuration
@AllArgsConstructor
public class RecordStepConfig {
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step recordStep(FlatFileItemReader<Record> reader, RecordWriter writer) {
        return stepBuilderFactory.get("recordStep")
                .<Record, Record>chunk(100)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<Record> recordReader(JobParams jobParams) {
        return new FlatFileItemReaderBuilder<Record>()
                .name("recordItemReader")
                .resource(new FileSystemResource(jobParams.getInputDir() + "/Проводки.txt"))
                .recordSeparatorPolicy(new RecordSeparatorPolicy())
                .lineMapper((s, i) -> {
                    var values = Split.splitAndConvert(s, "ldllmlmslml");
                    return new Record()
                            .setId((Long) values.get(0))
                            .setDate((Instant) values.get(1))
                            .setUserId((Long) values.get(2))
                            .setAccountFromId((Long) values.get(3))
                            .setMoney((BigDecimal) values.get(4))
                            .setAccountBudgetId((Long) values.get(5))
                            .setBudget((BigDecimal) values.get(6))
                            .setNote((String) values.get(7))
                            .setAccountToId((Long) values.get(8))
                            .setTransfer((BigDecimal) values.get(9));
                })
                .build();
    }

    @StepScope
    @Component
    @AllArgsConstructor
    @Slf4j
    public static class RecordWriter implements ItemWriter<Record> {
        private final SharedData sharedData;
        private final NamedParameterJdbcOperations operations;

        @Override
        public void write(List<? extends Record> items) {
            for (var record : items) {
                log.info("Insert: {}", record.getId());

                var uid = insertRecord(record);

                if (record.getAccountBudgetId() == null && record.getAccountToId() == null)
                    record.setAccountBudgetId(record.getAccountFromId());

                if (record.getAccountBudgetId() != null && record.getBudget() == null)
                    record.setBudget(record.getMoney());

                if (record.getAccountBudgetId() != null)
                    insertPartsSimple(record, uid);
                else if (record.getTransfer() == null)
                    insertPartsTransfer(record, uid);
                else
                    insertPartsExchange(record, uid);
            }
        }

        // Обмен валюты
        private void insertPartsExchange(Record record, UUID uid) {
            insertPart(uid, 1, record.getMoney(), record.getAccountFromId(), true);
            insertPart(uid, 2, record.getMoney(), record.getAccountFromId(), false);
            insertPart(uid, 3, record.getTransfer().negate(), record.getAccountToId(), true);
            insertPart(uid, 4, record.getTransfer().negate(), record.getAccountToId(), false);
        }

        // Перевод в одной валюте
        private void insertPartsTransfer(Record record, UUID uid) {
            int no = 1;
            if (!BigDecimals.equalsValue(record.getMoney(), BigDecimal.ZERO)) {
                insertPart(uid, no++, record.getMoney(), record.getAccountFromId(), true);
                insertPart(uid, no++, record.getMoney().negate(), record.getAccountToId(), true);
            }

            if (record.getBudget() != null && !BigDecimals.equalsValue(record.getBudget(), BigDecimal.ZERO)) {
                insertPart(uid, no++, record.getBudget(), record.getAccountFromId(), false);
                insertPart(uid, no, record.getBudget().negate(), record.getAccountToId(), false);
            }
        }

        // Простая проводка
        private void insertPartsSimple(Record record, UUID uid) {
            Assert.isTrue(record.getAccountToId() == null, "AccountTo is not null: " + record.getId());
            insertPart(uid, 1, record.getMoney(), record.getAccountFromId(), true);
            insertPart(uid, 2, record.getBudget(), record.getAccountBudgetId(), false);
        }

        private Long checkAndInsertAccount(Long accountId, boolean isMoney) {
            var account = sharedData.getAccount(accountId);
            if (account.isMoney() == isMoney)
                return accountId;

            var newId = (isMoney ? 1000 : 2000) + accountId;
            if (sharedData.getAccount(newId) != null)
                return newId;

            var newAccount = new Account()
                    .setId(newId)
                    .setMoney(isMoney)
                    .setName(account.getName() + (isMoney ? " Д" : " Б"))
                    .setClosed(account.isClosed())
                    .setCurrencyId(account.getCurrencyId());
            sharedData.addAccount(newAccount);

            operations.update("insert into account(id, name, closed, is_money, currency_id, total) " +
                            "values(:id, :name, :closed, :is_money, :currency_id, 0)",
                    Map.of("id", newId,
                            "name", newAccount.getName(),
                            "closed", newAccount.isClosed(),
                            "is_money", isMoney,
                            "currency_id", newAccount.getCurrencyId()));

            return newId;
        }

        private void insertPart(UUID uid, int no, BigDecimal money, Long accountId, boolean isMoney) {
            accountId = checkAndInsertAccount(accountId, isMoney);

            operations.update("insert into record_part(id, record_id, account_id, money, no) " +
                            "values(nextval('hibernate_sequence'), :record_id, :account_id, :money, :no)",
                    Map.of("record_id", uid,
                            "account_id", accountId,
                            "money", money,
                            "no", no));
        }


        private UUID insertRecord(Record record) {
            var uid = UUID.randomUUID();
            String note = "[" + record.getId() + "] " + Objects.requireNonNullElse(record.getNote(), "");

            operations.update("insert into record(id, time, note, creator_id, validated) values(:id, :time, :note, :creator_id, false)",
                    Map.of("id", uid,
                            "time", Timestamp.from(record.getDate()),
                            "note", note,
                            "creator_id", record.getUserId()));
            return uid;
        }
    }

    /// access передает абзацы "как есть". В результате получается
    // ...;"abcde
    // fgh";...
    // Склеиваем такое
    private static class RecordSeparatorPolicy extends SimpleRecordSeparatorPolicy {
        @Override
        public boolean isEndOfRecord(String line) {
            return StringUtils.countOccurrencesOf(line, "\"") % 2 == 0;
        }
    }
}
