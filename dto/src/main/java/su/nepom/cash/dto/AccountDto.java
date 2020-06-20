package su.nepom.cash.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Кошелек
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class AccountDto {
    private long id;
    private String name;
    private boolean closed; // закрыт (должен иметь нулевой остаток)
    private boolean money; // true - типа "деньги" (иначе - типа "бюджет")
    private String note;
    private BigDecimal total = BigDecimal.ZERO; // текущий остаток по кошельку
    private long currency;

    public AccountDto(long id) {
        this.id = id;
    }
}
