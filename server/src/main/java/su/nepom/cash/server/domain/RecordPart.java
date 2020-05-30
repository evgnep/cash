package su.nepom.cash.server.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import su.nepom.util.BigDecimals;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Часть проводка.
 * <p>Может быть создана или изменена без связи с сервером. Реплицируется вместе с проводкой
 */
@Data
@Accessors(chain = true)
@Entity
public class RecordPart {
    // !!! Внимание! Ручной equals
    @Id
    @GeneratedValue
    private long id;
    private int no;
    private BigDecimal money;
    private String note;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="account_id")
    private Account account;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="record_id")
    @ToString.Exclude
    private Record record;
    // !!! Внимание! Ручной equals

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordPart that = (RecordPart) o;
        return id == that.id &&
                no == that.no &&
                BigDecimals.equalsValue(money, that.money) &&
                Objects.equals(note, that.note) &&
                Objects.equals(account, that.account) &&
                Objects.equals(record.getId(), that.record.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, no, BigDecimals.hashCodeValue(money), note, account.getId(), record.getId());
    }
}
