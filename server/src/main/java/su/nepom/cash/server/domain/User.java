package su.nepom.cash.server.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Пользователь приложения.
 * <p>Создается только в серверной БД
 */
@Data
@Accessors(chain = true)
@Entity
@Table(name = "appl_user")
public class User {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private boolean isChild; // ребенок - права ограничены

    public User() {}

    public User(long id) {
        this.id = id;
    }
}
