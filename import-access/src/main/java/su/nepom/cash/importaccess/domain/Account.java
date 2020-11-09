package su.nepom.cash.importaccess.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Account {
    private long id;
    private String originalName;
    private String name;
    private long currencyId;
    private boolean closed;
    private boolean money;

    public Account setOriginalName(String v) {
        originalName = v;
        name = v;
        return this;
    }
}
