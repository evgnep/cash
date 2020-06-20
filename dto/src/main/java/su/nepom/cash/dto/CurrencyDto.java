package su.nepom.cash.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Валюта
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class CurrencyDto {
    private long id;
    private String name;
    private String code;

    public CurrencyDto(long id) {
        this.id = id;
    }
}
