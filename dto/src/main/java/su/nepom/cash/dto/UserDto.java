package su.nepom.cash.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Пользователь приложения.
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UserDto {
    private long id;
    private String name;
    private boolean child; // ребенок - права ограничены
    private String password;
    private boolean enabled;

    public UserDto(long id) {
        this.id = id;
    }
}
