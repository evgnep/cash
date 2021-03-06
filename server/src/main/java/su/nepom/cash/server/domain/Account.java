package su.nepom.cash.server.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;

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
    private boolean availableToChild; // true - доступен ребенку (видит операции, может делать операции, видит остаток)
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
