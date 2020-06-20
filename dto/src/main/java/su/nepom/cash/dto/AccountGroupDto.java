package su.nepom.cash.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

/**
 * Группа кошельков
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AccountGroupDto {
    private long id;
    private String name;
    private Set<Long> accounts = new HashSet<>();

    public AccountGroupDto(long id) {
        this.id = id;
    }
}
