package su.nepom.util;

import java.math.BigDecimal;

/// Утилиты для работы с BigDecimal
public class BigDecimals {
    /// Сравнение без учета scale (5.1 == 5.1000)
    public static boolean equalsValue(BigDecimal a, BigDecimal b) {
        if (a == null && b == null)
            return true;
        if (a == null || b == null)
            return false;
        return a.compareTo(b) == 0;
    }

    /// hashCode без учета scale
    public static int hashCodeValue(BigDecimal a) {
        if(a == null) {
            return 0;
        }
        return Double.hashCode(a.doubleValue());
    }
}
