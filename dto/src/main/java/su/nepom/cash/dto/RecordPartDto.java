package su.nepom.cash.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Часть проводки
 */
@Data
@Accessors(chain = true)
public class RecordPartDto {
    private long id;
    private int no;
    private BigDecimal money;
    private String note;
    private long account;
}
