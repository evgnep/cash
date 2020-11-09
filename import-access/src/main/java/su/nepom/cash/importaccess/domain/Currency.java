package su.nepom.cash.importaccess.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Currency {
    private long id;
    private String name;
    private String code;
}
