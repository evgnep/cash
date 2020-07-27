package su.nepom.cash.server.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import su.nepom.cash.server.repository.AccountRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SqlWithoutWhere")
@DisplayName("CRUD для Account")
class AccountTest extends DomainTest {
    private Currency currency1 = new Currency().setName("Rubles").setCode("Rb");
    private Currency currency2 = new Currency().setName("US$").setCode("Usd");
    private Account account = new Account().setName("Cash").setNote("Some note");

    @Autowired
    AccountRepository repository; // реп не проверяем - он не содержит кастомных методов

    @BeforeEach
    void beforeEach() {
        currency1 = manager.persist(currency1);
        currency2 = manager.persist(currency2);
        account.setCurrency(currency1);
    }

    @Test
    void create() {
        var id = manager.persistAndFlush(account).getId();
        manager.clear();

        var readed = manager.find(Account.class, id);
        assertThat(readed).isEqualTo(account);
        assertThat(readed.getCurrency()).isEqualTo(currency1);
    }

    @Test
    void update() {
        account = manager.persistAndFlush(account);
        manager.clear();

        account.setCurrency(currency2).setName("hsaC").setNote(null).setClosed(true).setMoney(true);
        manager.merge(account);
        manager.flush();
        manager.clear();

        var readed = manager.find(Account.class, account.getId());
        assertThat(readed).isEqualTo(account);
        assertThat(readed.getName()).isEqualTo("hsaC");
    }

    @Test
    void delete() {
        account = manager.persistAndFlush(account);
        manager.remove(account);
        manager.flush();
    }

    @Test
    void shouldPermitCloseWhenTotalIs0() {
        account = manager.persistAndFlush(account);

        account.setClosed(true);
        manager.flush();

        manager.clear();

        var readed = manager.find(Account.class, account.getId());
        assertThat(readed.isClosed()).isTrue();
    }

    @Test
    void shouldProhibitCloseWhenTotalIsNot0() {
        account = manager.persistAndFlush(account);

        manager.getEntityManager().createNativeQuery("update account set total = 42").executeUpdate();

        account.setClosed(true);
        manager.flush();

        manager.clear();

        var readed = manager.find(Account.class, account.getId());
        assertThat(readed.isClosed()).isFalse();
    }

    @Test
    void shouldOpenWhenTotalBecameNot0() {
        account.setClosed(true);
        account = manager.persistAndFlush(account);

        manager.getEntityManager().createNativeQuery("update account set total = 42").executeUpdate();

        manager.clear();

        var readed = manager.find(Account.class, account.getId());
        assertThat(readed.isClosed()).isFalse();
    }
}
