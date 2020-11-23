package su.nepom.cash.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Данные фискального чека (возвращенные из ОФД)
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ChequeDto {
    private final List<Item> items = new ArrayList<>();
    private Instant time;
    private String retailPlaceAddress;
    private String retailPlace;
    private BigDecimal cash;
    private BigDecimal ecash;

    public void addItem(String name, Double quantity, BigDecimal sum) {
        for (Item item : items)
            if (item.name.equals(name)) {
                item.quantity += quantity;
                item.sum = item.sum.add(sum);
                return;
            }

        items.add(new Item().setName(name).setQuantity(quantity).setSum(sum));
    }

    @Data
    @Accessors(chain = true)
    @NoArgsConstructor
    public static class Item {
        private String name;
        private Double quantity;
        private BigDecimal sum;
    }
}
