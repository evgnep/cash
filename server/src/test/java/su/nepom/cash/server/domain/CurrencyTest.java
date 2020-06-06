package su.nepom.cash.server.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import su.nepom.cash.server.repository.CurrencyRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CRUD для Currency")
class CurrencyTest extends DomainTest {
    private Currency currency = new Currency().setName("Rubles").setCode("Rb");

    @Autowired
    CurrencyRepository repository; // реп не проверяем - он не содержит кастомных методов

    @Test
    void create() {
        var id = manager.persistAndFlush(currency).getId();
        manager.clear();

        var readed = manager.find(Currency.class, id);
        assertThat(readed).isEqualTo(currency);
    }

    @Test
    void update() {
        currency = manager.persistAndFlush(currency);

        currency.setCode("$").setName("usd");
        manager.persistAndFlush(currency);
        manager.clear();

        var readed = manager.find(Currency.class, currency.getId());
        assertThat(readed).isEqualTo(currency);
        assertThat(readed.getName()).isEqualTo("usd");
    }

    @Test
    void delete() {
        currency = manager.persistAndFlush(currency);
        manager.remove(currency);
        manager.flush();
    }
}
