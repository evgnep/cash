package su.nepom.cash.importaccess.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Record {
    private long id;
    private Instant date;
    private long userId;
    private long accountFromId;
    private BigDecimal money;
    private Long accountBudgetId;
    private BigDecimal budget;
    private String note;
    private Long accountToId;
    private BigDecimal transfer;
}
