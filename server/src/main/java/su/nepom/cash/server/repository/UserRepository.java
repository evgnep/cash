package su.nepom.cash.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import su.nepom.cash.server.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
