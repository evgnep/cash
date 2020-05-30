package su.nepom.cash.server.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import su.nepom.cash.server.repository.RecordRepository;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CRUD для Record")
class RecordTest extends DomainTest {
    private Currency currency = new Currency().setName("Rubles").setCode("Rb");
    private User user1 = new User().setName("A");
    private User user2 = new User().setName("B");
    private Record record1 = new Record().setNote("Note").setTime(Instant.ofEpochSecond(42));
    private Account account = new Account().setName("Cash").setNote("Some note");
    private RecordPart recordPart1 = new RecordPart().setMoney(BigDecimal.valueOf(42)).setNo(1);
    private RecordPart recordPart2 = new RecordPart().setMoney(BigDecimal.valueOf(420)).setNo(2).setNote("X");


    @Autowired
    RecordRepository repository; // реп не проверяем - он не содержит кастомных методов

    @BeforeEach
    void beforeEach() {
        currency = manager.persist(currency);
        user1 = manager.persist(user1);
        user2 = manager.persist(user2);
        account.setCurrency(currency);
        account = manager.persist(account);
        record1.setCreator(user1);
        manager.flush();
        recordPart1.setAccount(account);
        recordPart2.setAccount(account);
    }

    @Test
    void create() {
        var id = manager.persistAndFlush(record1).getId();
        manager.clear();

        var readed = manager.find(Record.class, id);
        record1.setCreateTime(readed.getCreateTime()); // БД дает время по умолчанию
        assertThat(readed).isEqualTo(record1);
    }

    @Test
    void update() throws Exception {
        var id = manager.persistAndFlush(record1).getId();
        manager.clear();

        record1 = manager.find(Record.class, id);
        assertThat(record1.getUpdateTime()).isNull();

        Thread.sleep(200); // для проверки времени обновления (тригер в БД)

        record1.setCreator(user2).setNote(null).setValidated(true).setTime(Instant.ofEpochSecond(24));
        manager.flush();
        manager.clear();

        var readed = manager.find(Record.class, record1.getId());
        assertThat(readed.getUpdateTime()).isAfter(readed.getCreateTime()); // БД установит время обновления
        record1.setUpdateTime(readed.getUpdateTime());
        assertThat(readed).isEqualTo(record1);
    }

    @Test
    void delete() {
        var id = manager.persistAndFlush(record1).getId();
        manager.clear();

        record1 = manager.find(Record.class, id);
        manager.remove(record1);
        manager.flush();
    }

    @Test
    void workWithParts() {
        var logger = LoggerFactory.getLogger("TEST");
        logger.info("\n\nДобавим часть 1");
        record1 = manager.persist(record1);
        record1.addPart(recordPart1);
        var id = manager.persistAndFlush(record1).getId();
        manager.clear();

        logger.info("\n\nДобавим часть 2");
        var readed = manager.find(Record.class, id);
        assertThat(readed.getParts()).containsExactly(recordPart1);

        readed.addPart(recordPart2);
        manager.flush();
        manager.clear();

        logger.info("\n\nУдалим часть 2");
        readed = manager.find(Record.class, id);
        assertThat(readed.getParts()).containsExactly(recordPart1, recordPart2);

        readed.getParts().remove(1);
        manager.flush();
        manager.clear();

        logger.info("\n\nУдалим проводку");
        readed = manager.find(Record.class, id);
        assertThat(readed.getParts()).containsExactly(recordPart1);

        manager.remove(readed);
        manager.flush();
    }
}
