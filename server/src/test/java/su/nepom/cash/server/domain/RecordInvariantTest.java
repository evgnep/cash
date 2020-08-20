package su.nepom.cash.server.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionOperations;
import su.nepom.cash.server.DbConfig;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Проверка триггера check_record_invariant
 * Так как тот срабатывает в конце транзакции, то тест коммитит транзакции
 * Мне не удалось это сделать с DataJpaTest, поэтому поднимаю полный контекст
 */
@DisplayName("Record инвариант")
@SpringBootTest
@ActiveProfiles("test")
@Import(DbConfig.class)
class RecordInvariantTest {
    private final Currency currency1 = new Currency().setName("RUB").setCode("RUB");
    private final Currency currency2 = new Currency().setName("USD").setCode("USD");
    private final Account accountCash2 = new Account().setName("Cash2").setMoney(true);
    private final Account accountBudj2 = new Account().setName("Cat").setMoney(false);
    private final User user = new User().setName("A");
    private final Record record = new Record().setCreator(user).setTime(Instant.ofEpochSecond(42));
    @Autowired
    TransactionOperations transactionOperations;
    @Autowired
    EntityManager entityManager;
    private Account accountCash1 = new Account().setName("Cash1").setMoney(true);
    private Account accountBudj1 = new Account().setName("Food").setMoney(false);

    private void saveCurrencyAndUser() {
        entityManager.persist(currency1);
        entityManager.persist(currency2);
        entityManager.persist(user);
    }

    private void saveAccount(Account account) {
        if (account.getCurrency() == null)
            account.setCurrency(currency1);
        entityManager.persist(account);
    }

    private void saveAccounts() {
        saveAccount(accountCash1);
        saveAccount(accountCash2);
        saveAccount(accountBudj1);
        saveAccount(accountBudj2);
    }

    @AfterEach
    void afterEach() {
        transactionOperations.executeWithoutResult(s -> {
            entityManager.createNativeQuery("delete from record where true").executeUpdate();
            entityManager.createNativeQuery("delete from account where true").executeUpdate();
            entityManager.createNativeQuery("delete from appl_user where true").executeUpdate();
            entityManager.createNativeQuery("delete from currency where true").executeUpdate();
        });
    }

    @Test
    void Should_Ok_When_EqualTotalsAndOneCurrency() {
        transactionOperations.executeWithoutResult(s -> {
            saveCurrencyAndUser();
            saveAccounts();

            record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.TEN));
            record.addPart(new RecordPart().setAccount(accountBudj1).setMoney(BigDecimal.TEN));
            entityManager.persist(record);
        });
    }

    @Test
    void Should_Fail_When_DifTotals() {
        assertThatThrownBy(() ->
                transactionOperations.executeWithoutResult(s -> {
                    saveCurrencyAndUser();
                    saveAccounts();

                    record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.valueOf(42)));
                    record.addPart(new RecordPart().setAccount(accountBudj1).setMoney(BigDecimal.valueOf(10)));
                    entityManager.persist(record);
                }));
    }

    @Test
    void Should_Ok_When_EqualTotalsAndTwoCurrencies() {
        transactionOperations.executeWithoutResult(s -> {
            saveCurrencyAndUser();
            accountCash2.setCurrency(currency2);
            accountBudj2.setCurrency(currency2);
            saveAccounts();

            record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.valueOf(10)));
            record.addPart(new RecordPart().setAccount(accountBudj1).setMoney(BigDecimal.valueOf(10)));
            record.addPart(new RecordPart().setAccount(accountCash2).setMoney(BigDecimal.valueOf(20)));
            record.addPart(new RecordPart().setAccount(accountBudj2).setMoney(BigDecimal.valueOf(20)));
            entityManager.persist(record);
        });
    }

    @Test
    void Should_Ok_When_EqualTotalsAndThreeParts() {
        transactionOperations.executeWithoutResult(s -> {
            saveCurrencyAndUser();
            saveAccounts();

            record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.valueOf(100)));
            record.addPart(new RecordPart().setAccount(accountBudj1).setMoney(BigDecimal.valueOf(10)));
            record.addPart(new RecordPart().setAccount(accountBudj2).setMoney(BigDecimal.valueOf(90)));
            entityManager.persist(record);
        });
    }

    @Test
    void Should_Fail_When_DifTotalsAndTwoCurrencies() {
        assertThatThrownBy(() ->
                transactionOperations.executeWithoutResult(s -> {
                    saveCurrencyAndUser();
                    accountCash2.setCurrency(currency2);
                    accountBudj2.setCurrency(currency2);
                    saveAccounts();

                    record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.valueOf(20)));
                    record.addPart(new RecordPart().setAccount(accountBudj1).setMoney(BigDecimal.valueOf(10)));
                    record.addPart(new RecordPart().setAccount(accountCash2).setMoney(BigDecimal.valueOf(10)));
                    record.addPart(new RecordPart().setAccount(accountBudj2).setMoney(BigDecimal.valueOf(20)));
                    entityManager.persist(record);
                }));
    }

    @Test
    void Should_Fail_When_OnePart() {
        assertThatThrownBy(() ->
                transactionOperations.executeWithoutResult(s -> {
                    saveCurrencyAndUser();
                    saveAccounts();

                    record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.valueOf(20)));
                    entityManager.persist(record);
                }));
    }

    @Test
    void Should_Ok_When_UpdateAndEqualTotals() {
        transactionOperations.executeWithoutResult(s -> {
            saveCurrencyAndUser();
            saveAccounts();

            record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.valueOf(20)));
            record.addPart(new RecordPart().setAccount(accountBudj1).setMoney(BigDecimal.valueOf(20)));
            entityManager.persist(record);
        });

        transactionOperations.executeWithoutResult(s -> {
            var budj2 = entityManager.find(Account.class, accountBudj2.getId());
            var rec = entityManager.find(Record.class, record.getId());

            rec.getParts().get(0).setMoney(BigDecimal.valueOf(30));
            rec.addPart(new RecordPart().setAccount(budj2).setMoney(BigDecimal.valueOf(10)));
        });
    }

    @Test
    void Should_Fail_When_UpdateAndDiffTotals() {
        transactionOperations.executeWithoutResult(s -> {
            saveCurrencyAndUser();
            saveAccounts();

            record.addPart(new RecordPart().setAccount(accountCash1).setMoney(BigDecimal.valueOf(20)));
            record.addPart(new RecordPart().setAccount(accountBudj1).setMoney(BigDecimal.valueOf(20)));
            entityManager.persist(record);
        });

        assertThatThrownBy(() ->
                transactionOperations.executeWithoutResult(s -> {
                    var rec = entityManager.find(Record.class, record.getId());

                    rec.getParts().get(0).setMoney(BigDecimal.valueOf(30));
                }));
    }


}
