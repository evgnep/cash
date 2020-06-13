package su.nepom.cash.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;

/**
 * Группа кошельков
 */
@Data
@Accessors(chain = true)
public class AccountGroupDto {
    private long id;
    private String name;
    private Set<Long> accounts = new HashSet<>();

    public AccountGroupDto() {}

    public AccountGroupDto(long id) {
        this.id = id;
    }
}
