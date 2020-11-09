package su.nepom.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Кошелек
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AccountDto {
    private long id;
    private String name;
    private boolean closed; // закрыт (должен иметь нулевой остаток)
    private boolean money; // true - типа "деньги" (иначе - типа "бюджет")
    private boolean availableToChild; // true - доступен ребенку (видит операции, может делать операции, видит остаток)
    private String note;
    private BigDecimal total = BigDecimal.ZERO; // текущий остаток по кошельку
    private long currency;

    public AccountDto(long id) {
        this.id = id;
    }

    public AccountDto createCopy() {
        return toBuilder().build();
    }
}
