package su.nepom.cash.server.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Валюта.
 * <p>Создается только в серверной БД
 */
@Data
@Accessors(chain = true)
@Entity
public class Currency {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private String code;

    public Currency() {}

    public Currency(long id) {
        this.id = id;
    }
}
