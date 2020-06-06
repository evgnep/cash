package su.nepom.cash.server.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import su.nepom.util.BigDecimals;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Счет.
 * <p>Создается и меняется только в серверной БД
 */
@Data
@Accessors(chain = true)
@Entity
public class Account {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private boolean closed; // закрыт (должен иметь нулевой остаток)
    private boolean isMoney; // true - типа "деньги" (иначе - типа "бюджет")
    private String note;
    @Column(updatable = false)
    @EqualsAndHashCode.Exclude
    private BigDecimal total = BigDecimal.ZERO; // текущий остаток по кошельку. Обновляется на стороне БД
    @ManyToOne
    @JoinColumn(name="currency_id")
    private Currency currency;

    public Account() {
    }

    public Account(long id) {
        this.id = id;
    }
}
