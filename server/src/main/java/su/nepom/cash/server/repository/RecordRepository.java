package su.nepom.cash.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.nepom.cash.server.domain.AccountGroup;
import su.nepom.cash.server.domain.Record;

import java.util.UUID;

public interface RecordRepository extends JpaRepository<Record, UUID> {
}
