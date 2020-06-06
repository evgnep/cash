package su.nepom.cash.server.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import su.nepom.cash.server.repository.AccountGroupRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CRUD для AccountGroup")
class AccountGroupTest extends DomainTest {
    @Autowired
    AccountGroupRepository repository; // реп не проверяем - он не содержит кастомных методов
    private Currency currency = new Currency().setName("Rubles").setCode("Rb");
    private Account account1 = new Account().setName("Cash");
    private Account account2 = new Account().setName("Bank");
    private AccountGroup group1 = new AccountGroup().setName("X");

    @BeforeEach
    void beforeEach() {
        currency = manager.persist(currency);
        account1.setCurrency(currency);
        account2.setCurrency(currency);
        account1 = manager.persist(account1);
        account2 = manager.persist(account2);
    }

    @Test
    void create_empty() {
        var id = manager.persistAndFlush(group1).getId();
        manager.clear();

        var readed = manager.find(AccountGroup.class, id);
        assertThat(readed).isEqualTo(group1);
    }

    @Test
    void create_withAccounts() {
        group1.addAccount(account1);
        group1.addAccount(account2);
        var id = manager.persistAndFlush(group1).getId();
        manager.clear();

        var readed = manager.find(AccountGroup.class, id);
        assertThat(readed).isEqualTo(group1);
    }

    @Test
    void create_remove_add() {
        group1.addAccount(account1);
        group1.addAccount(account2);
        var id = manager.persistAndFlush(group1).getId();
        manager.clear();

        var readed = manager.find(AccountGroup.class, id);
        readed.removeAccount(account1.getId());
        readed.removeAccount(account2.getId());

        readed.addAccount(account2);
        manager.flush();
        manager.clear();

        readed = manager.find(AccountGroup.class, id);
        group1.removeAccount(account1.getId());
        assertThat(readed).isEqualTo(group1);
    }

    @Test
    void update() {
        group1.addAccount(account1);
        var id = manager.persistAndFlush(group1).getId();
        manager.clear();

        group1 = manager.merge(group1);
        group1.setName("Q");
        group1.addAccount(account2);
        manager.flush();
        manager.clear();

        var readed = manager.find(AccountGroup.class, id);
        assertThat(readed).isEqualTo(group1);
        assertThat(readed.getName()).isEqualTo("Q");
    }


    @Test
    void delete() {
        group1.addAccount(account1);
        manager.persistAndFlush(group1);
        manager.clear();

        group1 = manager.merge(group1);
        manager.remove(group1);
        manager.flush();
    }
}
