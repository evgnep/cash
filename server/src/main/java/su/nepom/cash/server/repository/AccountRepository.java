package su.nepom.cash.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.nepom.cash.server.domain.Account;
import su.nepom.cash.server.domain.Currency;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
