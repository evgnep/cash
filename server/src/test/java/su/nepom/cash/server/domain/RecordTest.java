package su.nepom.cash.server.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import su.nepom.cash.server.repository.AccountRepository;
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
    private Account account1 = new Account().setName("Cash").setNote("Some note").setAvailableToChild(true);
    @Autowired
    AccountRepository accountRepository;
    private final RecordPart recordPart1 = new RecordPart().setMoney(BigDecimal.valueOf(42)).setNo(1);
    private final RecordPart recordPart2 = new RecordPart().setMoney(BigDecimal.valueOf(420)).setNo(2).setNote("X");
    private final static Logger log = LoggerFactory.getLogger("TEST");

    @Autowired
    RecordRepository repository;
    private Account account2 = new Account().setName("Card").setNote("Other note").setAvailableToChild(false);

    @BeforeEach
    void beforeEach() {
        currency = manager.persist(currency);
        user1 = manager.persist(user1);
        user2 = manager.persist(user2);
        account1.setCurrency(currency);
        account1 = manager.persist(account1);
        account2.setCurrency(currency);
        account2 = manager.persist(account2);
        record1.setCreator(user1);
        manager.flush();
        recordPart1.setAccount(account1);
        recordPart2.setAccount(account1);
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
        log.info("\n\nДобавим часть 1");
        record1 = manager.persist(record1);
        record1.addPart(recordPart1);
        var id = manager.persistAndFlush(record1).getId();
        manager.clear();

        log.info("\n\nДобавим часть 2");
        var readed = manager.find(Record.class, id);
        assertThat(readed.getParts()).containsExactly(recordPart1);

        readed.addPart(recordPart2);
        manager.flush();
        manager.clear();

        log.info("\n\nУдалим часть 2");
        readed = manager.find(Record.class, id);
        assertThat(readed.getParts()).containsExactly(recordPart1, recordPart2);

        readed.getParts().remove(1);
        manager.flush();
        manager.clear();

        log.info("\n\nУдалим проводку");
        readed = manager.find(Record.class, id);
        assertThat(readed.getParts()).containsExactly(recordPart1);

        manager.remove(readed);
        manager.flush();
    }

    @Test
    void findByFilter() {
        log.info("\n\nTEST");
        record1.addPart(recordPart1).addPart(recordPart2);
        record1 = manager.persist(record1);

        var record2 = new Record().setNote("Note2").setTime(Instant.ofEpochSecond(500)).setCreator(user2)
                .addPart(new RecordPart().setMoney(BigDecimal.TEN).setNo(1).setAccount(account2))
                .addPart(new RecordPart().setMoney(BigDecimal.ZERO).setNo(2).setAccount(account1));

        record2 = manager.persist(record2);

        manager.flush();

        log.info("\n\nquery 1 - счет 1 - обе, без диапазона");
        var res = repository.findByFilter(account1.getId(), null, null, false, Pageable.unpaged());
        assertThat(res).containsExactlyInAnyOrder(record1, record2);

        log.info("\n\nquery 2 - счет 2 - вторая");
        res = repository.findByFilter(account2.getId(), null, null, false, Pageable.unpaged());
        assertThat(res).containsExactly(record2);

        log.info("\n\nquery 3 - диапазон дат");
        res = repository.findByFilter(account1.getId(), Instant.ofEpochSecond(0), Instant.ofEpochSecond(100), false, Pageable.unpaged());
        assertThat(res).containsExactlyInAnyOrder(record1);

        log.info("\n\nquery 4 - начало диапазона");
        res = repository.findByFilter(account1.getId(), Instant.ofEpochSecond(400), null, false, Pageable.unpaged());
        assertThat(res).containsExactlyInAnyOrder(record2);

        log.info("\n\nquery 5 - конец диапазона");
        res = repository.findByFilter(account1.getId(), null, Instant.ofEpochSecond(400), false, Pageable.unpaged());
        assertThat(res).containsExactlyInAnyOrder(record1);
    }

    @Test
    void findByFilterForChild() {
        log.info("\n\nTEST");
        record1.addPart(recordPart1).addPart(recordPart2);
        record1 = manager.persist(record1);

        var record2 = new Record().setNote("Note2").setTime(Instant.ofEpochSecond(500)).setCreator(user2)
                .addPart(new RecordPart().setMoney(BigDecimal.TEN).setNo(1).setAccount(account2)) // account2 недоступен ребенку
                .addPart(new RecordPart().setMoney(BigDecimal.ZERO).setNo(2).setAccount(account2));
        manager.persist(record2); // соответственно вся проводка недоступна

        manager.flush();

        var res = repository.findByFilter(account1.getId(), null, null, true, Pageable.unpaged());
        assertThat(res).containsExactlyInAnyOrder(record1);
    }

    @Test
    void saveThroughRepository() {
        record1.addPart(recordPart1).addPart(recordPart2);
        manager.persist(record1);
        manager.flush();
        manager.clear();

        log.warn("\n\n1");
        record1.getParts().remove(1);
        repository.save(record1);

        manager.flush();
        manager.clear();

        log.warn("\n\n2");
        record1 = repository.findById(record1.getId()).orElseThrow();
        assertThat(record1.getParts()).containsExactly(recordPart1);
    }

    @Test
    void Should_UpdateAccTotal_When_InsertNewRecord() {
        log.warn("\n\nCreate");
        recordPart2.setAccount(account2);
        record1.addPart(recordPart1).addPart(recordPart2);
        manager.persistAndFlush(record1);

        manager.clear();
        account1 = accountRepository.findById(account1.getId()).orElseThrow();
        assertThat(account1.getTotal()).isEqualTo("42.00");

        account2 = accountRepository.findById(account2.getId()).orElseThrow();
        assertThat(account2.getTotal()).isEqualTo("420.00");
    }

    @Test
    void Should_UpdateAccTotal_When_UpdateRecordPartMoney() {
        log.warn("\n\nCreate");
        recordPart2.setAccount(account2);
        record1.addPart(recordPart1).addPart(recordPart2);
        manager.persistAndFlush(record1);

        log.warn("\n\nUpdate");
        recordPart1.setMoney(BigDecimal.valueOf(500));
        recordPart2.setMoney(BigDecimal.valueOf(-600));
        manager.persistAndFlush(record1);

        manager.clear();
        log.warn("\n\nSelect");
        account1 = accountRepository.findById(account1.getId()).orElseThrow();
        assertThat(account1.getTotal()).isEqualTo("500.00");

        account2 = accountRepository.findById(account2.getId()).orElseThrow();
        assertThat(account2.getTotal()).isEqualTo("-600.00");
    }

    @Test
    void Should_UpdateAccTotal_When_UpdateRecordPartAccount() {
        log.warn("\n\nCreate");
        record1.addPart(recordPart1).addPart(recordPart2);
        manager.persistAndFlush(record1);

        log.warn("\n\nUpdate");
        recordPart2.setAccount(account2);
        manager.persistAndFlush(record1);

        manager.clear();
        log.warn("\n\nSelect");
        account1 = accountRepository.findById(account1.getId()).orElseThrow();
        assertThat(account1.getTotal()).isEqualTo("42.00");

        account2 = accountRepository.findById(account2.getId()).orElseThrow();
        assertThat(account2.getTotal()).isEqualTo("420.00");
    }
}
