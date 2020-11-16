package su.nepom.cash.server.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.Record;
import su.nepom.cash.server.domain.RecordPart;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class DbStatHealthIndicator implements HealthIndicator {
    private final EntityManager manager;

    private long count(Class<?> entity) {
        return (Long) manager.createQuery("select count(t) from " + entity.getName() + " t").getSingleResult();
    }

    private long newRecordsLastWeek() {
        return (Long) manager.createQuery("select count(r) from Record r where r.createTime > :start")
                .setParameter("start", Instant.now().minus(7, ChronoUnit.DAYS))
                .getSingleResult();
    }

    @Override
    public Health health() {
        return Health.up()
                .withDetail("Счетов", count(Account.class))
                .withDetail("Проводок", count(Record.class))
                .withDetail("Элементов проводок", count(RecordPart.class))
                .withDetail("Новых проводок за неделю", newRecordsLastWeek())
                .build();
    }
}
