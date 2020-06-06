package su.nepom.cash.server.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import su.nepom.cash.server.repository.CurrencyRepository;
import su.nepom.cash.server.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CRUD для User")
class UserTest extends DomainTest {
    private User user = new User().setName("Vasya");

    @Autowired
    UserRepository repository; // реп не проверяем - он не содержит кастомных методов

    @Test
    void create() {
        var id = manager.persistAndFlush(user).getId();
        manager.clear();

        var readed = manager.find(User.class, id);
        assertThat(readed).isEqualTo(user);
    }

    @Test
    void update() {
        user = manager.persistAndFlush(user);

        user.setName("42").setChild(true);
        manager.persistAndFlush(user);
        manager.clear();

        var readed = manager.find(User.class, user.getId());
        assertThat(readed).isEqualTo(user);
        assertThat(readed.getName()).isEqualTo("42");
    }

    @Test
    void delete() {
        user = manager.persistAndFlush(user);
        manager.remove(user);
        manager.flush();
    }
}
