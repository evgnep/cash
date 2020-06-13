package su.nepom.cash.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Валюта
 */
@Data
@Accessors(chain = true)
public class CurrencyDto {
    private long id;
    private String name;
    private String code;

    public CurrencyDto() {}

    public CurrencyDto(long id) {
        this.id = id;
    }
}
