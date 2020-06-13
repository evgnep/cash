package su.nepom.cash.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Пользователь приложения.
 */
@Data
@Accessors(chain = true)
public class UserDto {
    private long id;
    private String name;
    private boolean isChild; // ребенок - права ограничены

    public UserDto() {}

    public UserDto(long id) {
        this.id = id;
    }
}
