package su.nepom.cash.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import su.nepom.cash.server.domain.Record;

import java.time.Instant;
import java.util.UUID;

public interface RecordRepository extends JpaRepository<Record, UUID> {
    /**
     * Выборка Record по фильтру
     *
     * @param account Account.id, должен быть обязательно
     * @param from    Дата начала. Если null - с самой первой
     * @param to      Дата конца. Если null - до самой последней
     */
    @Query("select distinct p.record from RecordPart p where p.account.id = :account and " +
            "function('betweenIfNotNull', p.record.time, " +
            "function('toTimestampTz', :from), function('toTimestampTz', :to)) = true")
    Page<Record> findByFilter(long account, Instant from, Instant to, Pageable pageable);
}
