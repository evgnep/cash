package su.nepom.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Валюта
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CurrencyDto {
    private long id;
    private String name;
    private String code;

    public CurrencyDto(long id) {
        this.id = id;
    }

    public CurrencyDto createCopy() {
        return toBuilder().build();
    }
}
