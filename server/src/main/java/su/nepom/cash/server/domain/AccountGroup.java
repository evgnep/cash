package su.nepom.cash.server.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Группа счетов.
 * <p>Создается и меняется только в серверной БД
 */
@Data
@Accessors(chain = true)
@Entity
public class AccountGroup {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "account_to_group", joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id"))
    @Setter(AccessLevel.NONE)
    private Set<Account> accounts = new HashSet<>();

    public AccountGroup addAccount(Account account) {
        accounts.add(account);
        return this;
    }

    public AccountGroup removeAccount(long accountId) {
        accounts.removeIf(a -> a.getId() == accountId);
        return this;
    }
}
