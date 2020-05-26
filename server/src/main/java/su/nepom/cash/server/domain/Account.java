package su.nepom.cash.server.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import su.nepom.util.BigDecimals;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Счет.
 * <p>Создается только в серверной БД
 */
@Data
@Accessors(chain = true)
@Entity
public class Account {
    //-- ВНИМАНИЕ! Ручной equals
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private boolean closed; // закрыт (должен иметь нулевой остаток)
    private boolean isMoney; // true - типа "деньги" (иначе - типа "бюджет")
    private String note;
    @Column(updatable = false)
    private BigDecimal total = BigDecimal.ZERO; // текущий остаток по кошельку. Обновляется на стороне БД
    @ManyToOne
    @JoinColumn(name="currency_id")
    private Currency currency;
    //-- ВНИМАНИЕ! Ручной equals

    public Account() {
    }

    public Account(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id == account.id &&
                closed == account.closed &&
                isMoney == account.isMoney &&
                Objects.equals(name, account.name) &&
                Objects.equals(note, account.note) &&
                BigDecimals.equalsValue(total, account.total) && // ручной equals ради этого :(
                Objects.equals(currency, account.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
