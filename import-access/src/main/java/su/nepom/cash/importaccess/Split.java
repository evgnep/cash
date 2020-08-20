package su.nepom.cash.importaccess;

import lombok.SneakyThrows;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Split {
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("d.M.y H:m:s")
            .withZone(ZoneId.of("+3"));
    private final static DecimalFormat decimalFormat;

    static {
        var symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        decimalFormat = new DecimalFormat();
        decimalFormat.setDecimalFormatSymbols(symbols);
        decimalFormat.setParseBigDecimal(true);
    }

    /**
     * Разделяет строку в формате access на части
     * <p>
     * Пример
     * 27960;;"аптека ""Черемуха""";0
     * ->
     * 27960, пустая_строка, аптека "Черемуха", 0
     */
    public static List<String> split(String src) {
        State state = State.WaitSemicolon;
        var res = new ArrayList<String>();

        var cur = new StringBuilder();
        for (int i = 0, len = src.length(); i < len; ++i) {
            var c = src.charAt(i);
            switch (state) {
                case WaitSemicolon:
                    if (c == ';') {
                        res.add(cur.toString());
                        cur.setLength(0);
                    } else if (c == '"')
                        state = State.WaitQuote;
                    else
                        cur.append(c);
                    break;
                case WaitQuote:
                    if (c == '"') {
                        if (i + 1 == len) {
                        } else if (src.charAt(i + 1) != '"') { // нет экранирующей кавычки
                            res.add(cur.toString());
                            cur.setLength(0);
                            state = State.WaitSemicolon;
                        } else // есть, добавляем только одну
                            cur.append(c);

                        i += 1; // пропускаем следующий
                    } else
                        cur.append(c);
                    break;
            }
        }
        res.add(cur.toString());
        return res;
    }

    /**
     * Разделяет строку в формате access на части и приводит типы
     *
     * @param types каждый символ - тип части. i - целое, s - строка, d - дата, m - деньги*
     */
    @SneakyThrows
    public static List<Object> splitAndConvert(String src, String types) {
        var res = new ArrayList<Object>();
        var parts = split(src);
        res.ensureCapacity(parts.size());

        for (int i = 0; i < parts.size(); ++i) {
            var part = parts.get(i);
            switch (types.charAt(i)) {
                case 's':
                    res.add(part);
                    break;
                case 'i':
                    if (part.isEmpty())
                        res.add(null);
                    else
                        res.add(Integer.valueOf(part));
                    break;
                case 'l':
                    if (part.isEmpty())
                        res.add(null);
                    else
                        res.add(Long.valueOf(part));
                    break;
                case 'd':
                    res.add(dateTimeFormatter.parse(part, Instant::from));
                    break;
                case 'm':
                    if (part.isEmpty())
                        res.add(null);
                    else
                        res.add(decimalFormat.parse(part.substring(0, part.length() - 2)));
                    break;
            }
        }

        return res;
    }

    private enum State {
        WaitSemicolon,
        WaitQuote
    }
}
